import {setKeycloakUserInformation} from '@/coreSlice.ts';
import {useAppDispatch} from '@/hooks.ts';
import Keycloak from 'keycloak-js';
import React, {useEffect, useRef} from 'react';
import axios from 'axios';

const keycloakConfig = {
    realm: 'journalist-accreditation',
    url: 'http://localhost:9001/',
    clientId: 'journalist-accreditation'
}

export const AuthContext: React.Context<Keycloak> = React.createContext(new Keycloak(keycloakConfig));

export default function AuthWrapper({children}: { children: React.ReactNode }) {
    const dispatch = useAppDispatch();
    const keycloakRef = useRef<Keycloak | null>(null);

    useEffect(() => {
        checkKeycloak().then();

        // Set up axios interceptor to handle 401 errors
        const responseInterceptor = axios.interceptors.response.use(
            response => response,
            async error => {
                const originalRequest = error.config;

                // If the error is 401 and we haven't tried to refresh the token yet
                if (error.response?.status === 401 && !originalRequest._retry && keycloakRef.current) {
                    originalRequest._retry = true;

                    try {
                        // Refresh the token
                        const refreshed = await keycloakRef.current.updateToken(30);

                        if (refreshed) {
                            console.log('Token refreshed');
                            // Update the authorization header with the new token
                            axios.defaults.headers.common['authorization'] = `Bearer ${keycloakRef.current.token}`;
                            originalRequest.headers['authorization'] = `Bearer ${keycloakRef.current.token}`;

                            // Retry the original request with the new token
                            return axios(originalRequest);
                        }
                    } catch (refreshError) {
                        console.error('Token refresh failed:', refreshError);
                        // If refresh fails, redirect to login
                        keycloakRef.current.login();
                        return Promise.reject(refreshError);
                    }
                }

                return Promise.reject(error);
            }
        );

        // Clean up interceptor on unmount
        return () => {
            axios.interceptors.response.eject(responseInterceptor);
        };
    }, []);

    const checkKeycloak = async () => {
        try {
            // Initialize Keycloak instance
            const keycloak = new Keycloak(keycloakConfig);
            keycloakRef.current = keycloak;

            // Set up token refresh callback
            keycloak.onTokenExpired = () => {
                console.log('Token expired, refreshing...');
                keycloak.updateToken(30).then(refreshed => {
                    if (refreshed) {
                        console.log('Token refreshed automatically');
                        // Update axios headers with the new token
                        axios.defaults.headers.common['authorization'] = `Bearer ${keycloak.token}`;
                    } else {
                        console.log('Token not refreshed, still valid');
                    }
                }).catch(error => {
                    console.error('Failed to refresh token:', error);
                    keycloak.login();
                });
            };

            const authenticated = await keycloak.init({
                onLoad: 'check-sso',
                checkLoginIframe: false, // Disable iframe checking for better performance
                pkceMethod: 'S256' // Use PKCE for better security
            });

            if (authenticated) {
                dispatch(setKeycloakUserInformation({
                        userUuid: keycloak.subject ?? '',
                        roles: keycloak.realmAccess!.roles,
                        authenticatedUserEmail: keycloak.idTokenParsed?.email ?? '',
                        authenticatedUserName: keycloak.idTokenParsed?.name ?? '',
                    }),
                );
                axios.defaults.baseURL = import.meta.env.VITE_BACKEND_URL;
                axios.defaults.headers.common['authorization'] = `Bearer ${keycloak.token}`;

                // Set up automatic token refresh before it expires
                keycloak.updateToken(70).then(); // Refresh token if it's less than 70% of its lifetime
            } else {
                keycloak.login().then(() => {
                    checkKeycloak().then();
                });
            }
        } catch (error) {
            console.error('Failed to initialize adapter:', error);
        }
    }

    return (
        <AuthContext.Provider value={keycloakRef.current || new Keycloak(keycloakConfig)}>
            {children}
        </AuthContext.Provider>
    )
}

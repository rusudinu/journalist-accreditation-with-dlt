import {setKeycloakUserInformation} from '@/coreSlice.ts';
import {useAppDispatch} from '@/hooks.ts';
import Keycloak from 'keycloak-js';
import React, {useEffect} from 'react';
import axios from 'axios';

const keycloakConfig = {
    realm: 'journalist-accreditation',
    url: 'http://localhost:9001/',
    clientId: 'journalist-accreditation'
}

export const AuthContext: React.Context<Keycloak> = React.createContext(new Keycloak(keycloakConfig));

export default function AuthWrapper({children}: { children: React.ReactNode }) {
    const dispatch = useAppDispatch();
    const keycloak = new Keycloak(keycloakConfig);

    useEffect(() => {
        checkKeycloak().then();
    }, []);

    const checkKeycloak = async () => {
        try {
            const authenticated = await keycloak.init(
                {
                    onLoad: 'check-sso'
                }
            );

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
        <AuthContext.Provider value={keycloak}>
            {children}
        </AuthContext.Provider>
    )
}

import { useAppSelector } from '@/hooks.ts';


export const useUserHasRole = (role: string): boolean => {
    return useAppSelector((state) => state.core.userRoles).includes(role);
}

export const useUserIsAuthenticated = (): boolean => {
    return useAppSelector((state) => state.core.authenticatedUserId) !== '';
}

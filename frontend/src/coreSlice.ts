import { createSlice } from '@reduxjs/toolkit'
import { v4 as uuidv4 } from 'uuid';

export interface CoreState {
    userUuid: string;
    authenticatedUserId: string;
    userRoles: string[];
}

const initialState: CoreState = {
    userUuid: new URLSearchParams(window.location.search).get('playerId') || uuidv4(),
    authenticatedUserId: '',
    userRoles: [],
}

export const coreSlice = createSlice({
    name: 'core',
    initialState,
    reducers: {
        setUserUuid: (state, action) => {
            state.userUuid = action.payload;
        },
        setAuthenticatedUserId: (state, action) => {
            state.authenticatedUserId = action.payload;
        },
        setUserRoles: (state, action) => {
            state.userRoles = action.payload;
        },
        setKeycloakUserInformation: (state, action: {
            payload: {
                userUuid: string,
                roles: string[]
            },
        }) => {
            state.authenticatedUserId = action.payload.userUuid;
            state.userRoles = action.payload.roles;
        },
    },
})

// Action creators are generated for each case reducer function
export const { setUserUuid, setAuthenticatedUserId, setUserRoles, setKeycloakUserInformation } = coreSlice.actions

export default coreSlice.reducer

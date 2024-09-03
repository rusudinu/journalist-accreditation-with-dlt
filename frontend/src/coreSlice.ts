import {createSlice} from '@reduxjs/toolkit'

export interface CoreState {
    authenticatedUserEmail: string;
    authenticatedUserName: string;
    authenticatedUserId: string;
    userRoles: string[];
}

const initialState: CoreState = {
    authenticatedUserEmail: '',
    authenticatedUserName: '',
    authenticatedUserId: '',
    userRoles: [],
}

export const coreSlice = createSlice({
    name: 'core',
    initialState,
    reducers: {
        setAuthenticatedUserId: (state, action) => {
            state.authenticatedUserId = action.payload;
        },
        setUserRoles: (state, action) => {
            state.userRoles = action.payload;
        },
        setKeycloakUserInformation: (state, action: {
            payload: {
                authenticatedUserEmail: string,
                authenticatedUserName: string,
                userUuid: string,
                roles: string[]
            },
        }) => {
            state.authenticatedUserId = action.payload.userUuid;
            state.userRoles = action.payload.roles;
            state.authenticatedUserEmail = action.payload.authenticatedUserEmail;
            state.authenticatedUserName = action.payload.authenticatedUserName;
        },
    },
})

// Action creators are generated for each case reducer function
export const {setAuthenticatedUserId, setUserRoles, setKeycloakUserInformation} = coreSlice.actions

export default coreSlice.reducer

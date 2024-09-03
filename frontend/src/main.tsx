import {createRoot} from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import {Provider} from "react-redux";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import {Toaster} from "@/components/ui/sonner.tsx";
import AuthWrapper from "@/hoc/AuthWrapper.tsx";
import React from 'react';
import {store} from "@/store.ts";
import Menu from "@/common/menu/Menu.tsx";

const router = createBrowserRouter([
    {
        path: '/',
        element: <Menu/>,
        children: [
            {
                path: '/',
                element: <App/>,
            },
        ],
    },
]);

createRoot(document.getElementById('root')!).render(
    <Provider store={store}>
        <AuthWrapper>
            <React.StrictMode>
                <RouterProvider router={router}/>
                <Toaster/>
            </React.StrictMode>
        </AuthWrapper>
    </Provider>
)

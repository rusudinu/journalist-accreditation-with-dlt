import {useUserHasRole, useUserIsAuthenticated} from '@/common/auth/UserUtils.ts';
import {NavigationMenu, NavigationMenuItem, NavigationMenuList, NavigationMenuTrigger, NavigationMenuContent, NavigationMenuLink} from '@/components/ui/navigation-menu.tsx';
import {cn} from '@/lib/utils.ts';
import React, {useContext, useEffect} from 'react';
import {Link, Outlet, useLocation} from 'react-router-dom';
import {useAppSelector} from "@/hooks.ts";
import {AuthContext} from "@/hoc/AuthWrapper.tsx";

const generalComponents = [
    {
        title: 'Home',
        href: '/home',
        description: 'View your own documents',
    },
    {
        title: 'Users',
        href: '/users',
        description: 'View all users',
    },
]

const adminComponents: { title: string, href: string, description: string }[] = [
    {
        title: 'Requests Without Approval Process',
        href: '/admin/requests-without-approval-process',
        description: 'View and assign approval processes to requests',
    },
    {
        title: 'Requests With Approval Process',
        href: '/admin/requests-with-approval-process',
        description: 'View requests with approval processes and their status',
    },
];

const pathsWhereMenuIsHidden: string[] = [];

const MenuComponent = () => {
    const keycloak = useContext(AuthContext);
    const location = useLocation();
    const hasAdminRole = useUserHasRole('ADMIN');
    const userIsAuthenticated = useUserIsAuthenticated();
    const [showMenu, setShowMenu] = React.useState(true);
    const [showLogout, setShowLogout] = React.useState(false);
    const authenticatedUserName = useAppSelector((state) => state.core.authenticatedUserName);

    useEffect(() => {
        setShowMenu(!pathsWhereMenuIsHidden.includes(location.pathname));
    }, [location]);

    const logout = () => {
        keycloak.logout().then();
    }

    return (
        showMenu ?
            <div className="relative max-w-screen-xl mx-auto">
                <div className="absolute top-0 z-50 left-0 right-0 bottom-0">
                    <NavigationMenu className="top-2">
                        <NavigationMenuList>
                            <NavigationMenuItem>
                                <NavigationMenuTrigger>General</NavigationMenuTrigger>
                                <NavigationMenuContent>
                                    <ul className="grid w-[400px] gap-3 p-4 md:w-[500px] md:grid-cols-2 lg:w-[600px]">
                                        {generalComponents.map((component) => (
                                            <ListItem
                                                key={component.title}
                                                title={component.title}
                                                to={component.href}
                                            >
                                                {component.description}
                                            </ListItem>
                                        ))}
                                    </ul>
                                </NavigationMenuContent>
                            </NavigationMenuItem>
                            {hasAdminRole && userIsAuthenticated && (
                                <NavigationMenuItem>
                                    <NavigationMenuTrigger>Admin</NavigationMenuTrigger>
                                    <NavigationMenuContent>
                                        <ul className="grid w-[400px] gap-3 p-4 md:w-[500px] md:grid-cols-2 lg:w-[600px]">
                                            {adminComponents.map((component) => (
                                                <ListItem
                                                    key={component.title}
                                                    title={component.title}
                                                    to={component.href}
                                                >
                                                    {component.description}
                                                </ListItem>
                                            ))}
                                        </ul>
                                    </NavigationMenuContent>
                                </NavigationMenuItem>
                            )}
                        </NavigationMenuList>
                    </NavigationMenu>
                    {userIsAuthenticated && (
                        <div
                            className="absolute top-0 right-0 p-4 flex items-center space-x-4"
                            onMouseEnter={() => setShowLogout(true)}
                            onMouseLeave={() => setShowLogout(false)}
                        >
                            <span className="text-sm font-medium">{authenticatedUserName}</span>
                            {showLogout && (
                                <button
                                    onClick={logout} // Replace with actual logout function
                                    className="text-sm text-red-600 hover:text-red-800"
                                >
                                    Logout
                                </button>
                            )}
                        </div>
                    )}
                </div>
                <div className="absolute left-0 right-0 top-14 bottom-0">
                    <div className="m-2">
                        <Outlet/>
                    </div>
                </div>
            </div>
            : <Outlet/>
    );
};

export default MenuComponent;

const ListItem = React.forwardRef<
    React.ElementRef<typeof Link>,
    React.ComponentPropsWithoutRef<typeof Link>
>(({className, title, children, ...props}, ref) => {
    return (
        <li>
            <NavigationMenuLink asChild>
                <Link
                    ref={ref}
                    className={cn(
                        'block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground',
                        className
                    )}
                    {...props}
                >
                    <div className="text-sm font-medium leading-none">{title}</div>
                    <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
                        {children}
                    </p>
                </Link>
            </NavigationMenuLink>
        </li>
    )
});
ListItem.displayName = 'ListItem';

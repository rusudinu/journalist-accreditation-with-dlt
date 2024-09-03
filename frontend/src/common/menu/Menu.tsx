import { useUserHasRole, useUserIsAuthenticated } from '@/common/auth/UserUtils.ts';
import { NavigationMenu, NavigationMenuItem, NavigationMenuList, NavigationMenuTrigger, NavigationMenuContent, NavigationMenuLink } from '@/components/ui/navigation-menu.tsx';
import { cn } from '@/lib/utils.ts';
import React, { useEffect } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';

const generalComponents = [
    {
        title: 'Packs',
        href: '/packs',
        description: 'View all packs',
    },
    {
        title: 'Cards',
        href: '/cards',
        description: 'Cards wiki',
    },
]

const adminComponents = [
    {
        title: 'Live games & lobbies',
        href: '/admin/permissions',
        description: 'View all live games & lobbies',
    },
];

const pathsWhereMenuIsHidden = ['/game-lobby', '/game'];

const MenuComponent = () => {
    const location = useLocation();
    const hasAdminRole = useUserHasRole('admin');
    const userIsAuthenticated = useUserIsAuthenticated();
    const [showMenu, setShowMenu] = React.useState(true);

    useEffect(() => {
        setShowMenu(!pathsWhereMenuIsHidden.includes(location.pathname));
    }, [location]);

    return (
        showMenu ? <div className="relative max-w-screen-xl mx-auto">
                <div className="absolute top-5 z-50 left-0 right-0 bottom-0">
                    <NavigationMenu>
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
                </div>
                <div className="absolute left-0 right-0 top-0 bottom-0">
                    <Outlet/>
                </div>
            </div>
            : <Outlet/>
    );
};

export default MenuComponent;

const ListItem = React.forwardRef<
    React.ElementRef<typeof Link>,
    React.ComponentPropsWithoutRef<typeof Link>
>(({ className, title, children, ...props }, ref) => {
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

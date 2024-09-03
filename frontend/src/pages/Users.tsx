import {useEffect, useState} from 'react';
import {Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {IUser} from "@/bemodel/Api.ts";
import axios from 'axios';

function Users() {
    const [users, setUsers] = useState<IUser[]>([]);

    useEffect(() => {
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/users`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setUsers(response.data);
            })
            .catch((error) => {
                console.error('Error:', error);
            });
    }, []);

    return (
        <>
            <Table>
                <TableCaption>A list of users.</TableCaption>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[100px]">ID</TableHead>
                        <TableHead>Keycloak ID</TableHead>
                        <TableHead>Created Date</TableHead>
                        <TableHead>Deleted</TableHead>
                        <TableHead>Document Count</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {users.map((user) => (
                        <TableRow key={user.id}>
                            <TableCell className="font-medium">{user.id}</TableCell>
                            <TableCell>{user.keycloakId}</TableCell>
                            <TableCell>{user.createdDate}</TableCell>
                            <TableCell>{user.deleted ? 'Yes' : 'No'}</TableCell>
                            <TableCell>{user.documents?.length ?? 0}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </>
    );
}

export default Users;

import {useEffect, useState} from 'react';
import {Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {IUserDTO} from "@/bemodel/Api.ts";
import axios from 'axios';
import {useNavigate} from 'react-router-dom';
import {Button} from "@/components/ui/button.tsx";

function Users() {
    const [users, setUsers] = useState<IUserDTO[]>([]);
    const navigate = useNavigate();

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

    const handleRedirect = (userId: string | undefined) => {
        if (userId !== undefined) {
            navigate(`/user/${userId}`);
        }
    };

    return (
        <>
            <Table>
                <TableCaption>The list of users and their documents.</TableCaption>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[100px]">ID</TableHead>
                        <TableHead>Document Count</TableHead>
                        <TableHead>Last Document Status</TableHead>
                        <TableHead>Action</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {users.map((user) => {
                        const lastDocument = user.documents?.[user.documents.length - 1];

                        return (
                            <TableRow key={user.id}>
                                <TableCell className="font-medium">{user.id}</TableCell>
                                <TableCell>{user.documents?.length ?? 0}</TableCell>
                                <TableCell>{lastDocument?.status || 'No documents'}</TableCell>
                                <TableCell>
                                    <Button
                                        onClick={() => handleRedirect(user.keycloakId ?? '')}
                                    >
                                        View Details
                                    </Button>
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
        </>
    );
}

export default Users;

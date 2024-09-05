import {useEffect, useState} from 'react';
import axios from 'axios';
import {IRequest} from "@/bemodel/Api.ts";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {Button} from "@/components/ui/button.tsx";
import {useNavigate} from "react-router-dom";
import {Badge} from "@/components/ui/badge.tsx";

function RequestsTable() {
    const navigate = useNavigate()
    const [requests, setRequests] = useState<IRequest[]>([]);

    useEffect(() => {
        fetchHomeFeed();
    }, []);

    const fetchHomeFeed = () => {
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setRequests(response.data);
            })
            .catch((error) => {
                console.error('Error:', error);
                setTimeout(() => {
                    fetchHomeFeed();
                }, 200);
            });
    }

    const openRequestPage = (request: IRequest) => {
        navigate(`/request/${request.id}`);
    }

    // if the user is a journalist, show a button to start a new request and the list of previous requests
    // if the user is juridic show a list of requests
    // if the user is director show a list of requests
    return (
        <>
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[100px]">Document ID</TableHead>
                        <TableHead>Uploaded documents</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Created Date</TableHead>
                        <TableHead>Action</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {requests.map((request) => (
                        <TableRow key={request.id}>
                            <TableCell className="font-medium">{request.id}</TableCell>
                            <TableCell>{request.documents?.length}</TableCell>
                            <TableCell><Badge variant={request.status}>{request.status}</Badge></TableCell>
                            <TableCell>{request.createdDate}</TableCell>
                            <TableCell>
                                <Button
                                    onClick={() => openRequestPage(request)}
                                >
                                    Open
                                </Button>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </>
    );
}

export default RequestsTable;

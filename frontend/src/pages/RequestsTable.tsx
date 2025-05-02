import {useEffect, useState} from 'react';
import axios from 'axios';
import {IRequest} from "@/bemodel/Api.ts";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {Button} from "@/components/ui/button.tsx";
import {useNavigate} from "react-router-dom";
import {Badge} from "@/components/ui/badge.tsx";
import {useAppSelector} from "@/hooks.ts";

function RequestsTable() {
    const navigate = useNavigate();
    const [reviewRequests, setReviewRequests] = useState<IRequest[]>([]);
    const userId = useAppSelector((state) => state.core.authenticatedUserId);

    useEffect(() => {
        if (userId) {
            fetchDocumentsThatNeedReview();
        }
    }, [userId]);

    const fetchDocumentsThatNeedReview = () => {
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/need-review`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setReviewRequests(response.data);
            })
            .catch((error) => {
                console.error('Error fetching review requests:', error);
            });
    }

    const openRequestPage = (request: IRequest) => {
        navigate(`/request/${request.id}`);
    }

    return (
        <>
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[100px]">Request ID</TableHead>
                        <TableHead>Uploaded documents</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Review Status</TableHead>
                        <TableHead>Created Date</TableHead>
                        <TableHead>Action</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {reviewRequests.map((request) => (
                        <TableRow key={request.id}>
                            <TableCell className="font-medium">{request.id}</TableCell>
                            <TableCell>{request.documents?.length}</TableCell>
                            <TableCell><Badge variant={request.status}>{request.status}</Badge></TableCell>
                            <TableCell>
                                <Badge variant="destructive">Needs Review</Badge>
                            </TableCell>
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

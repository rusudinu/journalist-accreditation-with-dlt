import {useEffect, useState} from 'react';
import axios from 'axios';
import {IRequest} from "@/bemodel/Api.ts";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {Button} from "@/components/ui/button.tsx";
import {useNavigate} from "react-router-dom";
import {Badge} from "@/components/ui/badge.tsx";
import {useAppSelector} from "@/hooks.ts";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs.tsx";

function RequestsTable() {
    const navigate = useNavigate();
    const [requests, setRequests] = useState<IRequest[]>([]);
    const [reviewRequests, setReviewRequests] = useState<IRequest[]>([]);
    const [reviews, setReviews] = useState<{id: number, approved: boolean | null, approvalStep: {approvalProcess: {id: number}}}[]>([]);
    const [, setActiveTab] = useState<string>("all");
    const userId = useAppSelector((state) => state.core.authenticatedUserId);

    useEffect(() => {
        fetchHomeFeed();
        if (userId) {
            fetchReviewRequests();
            fetchReviews();
        }
    }, [userId]);

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

    const fetchReviewRequests = () => {
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-reviews/reviewer/${userId}/requests`, {
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

    const fetchReviews = () => {
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-reviews/reviewer/${userId}`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setReviews(response.data);
            })
            .catch((error) => {
                console.error('Error fetching reviews:', error);
            });
    }

    const openRequestPage = (request: IRequest) => {
        navigate(`/request/${request.id}`);
    }

    // Check if a request has been reviewed by the current user
    const hasUserReviewed = (requestId: number) => {
        // Find all reviews for steps in this request
        const requestReviews = reviews.filter(review => {
            const stepApprovalProcess = review.approvalStep?.approvalProcess;
            if (!stepApprovalProcess) return false;

            // Check if this review's approval process is associated with the current request
            return reviewRequests.some(req => 
                req.id === requestId && 
                req.approvalProcess?.id === stepApprovalProcess.id
            );
        });

        // Check if any of these reviews have an approved status (not null)
        return requestReviews.some(review => review.approved !== null);
    }

    // if the user is a journalist, show a button to start a new request and the list of previous requests
    // if the user is juridic show a list of requests
    // if the user is director show a list of requests
    return (
        <>
            <Tabs defaultValue="all" className="w-full" onValueChange={setActiveTab}>
                <TabsList className="mb-4">
                    <TabsTrigger value="all">All Requests</TabsTrigger>
                    <TabsTrigger value="review">My Review Requests</TabsTrigger>
                </TabsList>

                <TabsContent value="all">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead className="w-[100px]">Request ID</TableHead>
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
                </TabsContent>

                <TabsContent value="review">
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
                                        {hasUserReviewed(request.id) ? (
                                            <Badge variant="success">Reviewed</Badge>
                                        ) : (
                                            <Badge variant="destructive">Needs Review</Badge>
                                        )}
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
                </TabsContent>
            </Tabs>
        </>
    );
}

export default RequestsTable;

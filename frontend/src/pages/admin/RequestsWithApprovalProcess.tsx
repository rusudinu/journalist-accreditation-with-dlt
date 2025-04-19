import { useEffect, useState } from 'react';
import axios from 'axios';
import { IRequestWithApprovalStatus } from "@/bemodel/ApprovalProcess.ts";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table.tsx";
import { Badge } from "@/components/ui/badge.tsx";
import { toast } from "sonner";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

function RequestsWithApprovalProcess() {
    const navigate = useNavigate();
    const [requests, setRequests] = useState<IRequestWithApprovalStatus[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        fetchRequestsWithApprovalProcess();
    }, []);

    const fetchRequestsWithApprovalProcess = () => {
        setLoading(true);
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests/with-approval-process`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setRequests(response.data);
                setLoading(false);
            })
            .catch((error) => {
                console.error('Error:', error);
                toast.error('Failed to fetch requests with approval process');
                setLoading(false);
            });
    }

    // Calculate progress percentage for the progress bar
    const calculateProgressPercentage = (progressDisplay: string | undefined) => {
        if (!progressDisplay) return 0;

        const [completed, total] = progressDisplay.split('/').map(Number);
        if (isNaN(completed) || isNaN(total) || total === 0) return 0;

        return (completed / total) * 100;
    };

    const handleViewDetails = (requestId: number) => {
        navigate(`/admin/request-approval-details/${requestId}`);
    };

    return (
        <div>
            <h1 className="text-2xl font-bold mb-4">Requests With Approval Process</h1>
            {loading ? (
                <div>Loading...</div>
            ) : requests.length === 0 ? (
                <div>No requests with approval process found.</div>
            ) : (
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[100px]">Request ID</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Created Date</TableHead>
                            <TableHead>User</TableHead>
                            <TableHead>Approval Process</TableHead>
                            <TableHead>Current Step</TableHead>
                            <TableHead>Progress</TableHead>
                            <TableHead>Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {requests.map((request) => (
                            <TableRow key={request.id}>
                                <TableCell className="font-medium">{request.id}</TableCell>
                                <TableCell><Badge variant={request.status}>{request.status}</Badge></TableCell>
                                <TableCell>{new Date(request.createdDate || '').toLocaleString()}</TableCell>
                                <TableCell>{request.userName}</TableCell>
                                <TableCell>{request.approvalProcessName}</TableCell>
                                <TableCell>{request.currentStepName || 'N/A'}</TableCell>
                                <TableCell>
                                    <div className="flex flex-col gap-2">
                                        <div className="text-sm">{request.progressDisplay}</div>
                                        <Progress value={calculateProgressPercentage(request.progressDisplay)} />
                                    </div>
                                </TableCell>
                                <TableCell>
                                    <Button 
                                        onClick={() => handleViewDetails(request.id!)}
                                        variant="outline"
                                        size="sm"
                                    >
                                        View Details
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            )}
        </div>
    );
}

export default RequestsWithApprovalProcess;

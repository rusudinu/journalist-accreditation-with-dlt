import { useEffect, useState } from 'react';
import axios from 'axios';
import { IRequest } from "@/bemodel/Api.ts";
import { IApprovalProcess } from "@/bemodel/ApprovalProcess.ts";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Badge } from "@/components/ui/badge.tsx";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";

function RequestsWithoutApprovalProcess() {
    const [requests, setRequests] = useState<IRequest[]>([]);
    const [approvalProcesses, setApprovalProcesses] = useState<IApprovalProcess[]>([]);
    const [selectedApprovalProcesses, setSelectedApprovalProcesses] = useState<Record<number, number>>({});
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        fetchRequestsWithoutApprovalProcess();
        fetchApprovalProcesses();
    }, []);

    const fetchRequestsWithoutApprovalProcess = () => {
        setLoading(true);
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests/without-approval-process`, {
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
                toast.error('Failed to fetch requests without approval process');
                setLoading(false);
            });
    }

    const fetchApprovalProcesses = () => {
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-processes`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setApprovalProcesses(response.data);
            })
            .catch((error) => {
                console.error('Error:', error);
                toast.error('Failed to fetch approval processes');
            });
    }

    const handleApprovalProcessChange = (requestId: number, approvalProcessId: string) => {
        setSelectedApprovalProcesses({
            ...selectedApprovalProcesses,
            [requestId]: parseInt(approvalProcessId)
        });
    }

    const assignApprovalProcess = (requestId: number) => {
        const approvalProcessId = selectedApprovalProcesses[requestId];
        if (!approvalProcessId) {
            toast.error('Please select an approval process');
            return;
        }

        axios.post(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests/${requestId}/assign-approval-process/${approvalProcessId}`, {}, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then(() => {
                toast.success('Approval process assigned successfully');
                fetchRequestsWithoutApprovalProcess(); // Refresh the list
            })
            .catch((error) => {
                console.error('Error:', error);
                toast.error('Failed to assign approval process');
            });
    }

    return (
        <div>
            <h1 className="text-2xl font-bold mb-4">Requests Without Approval Process</h1>
            {loading ? (
                <div>Loading...</div>
            ) : requests.length === 0 ? (
                <div>No requests without approval process found.</div>
            ) : (
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[100px]">Request ID</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Created Date</TableHead>
                            <TableHead>User</TableHead>
                            <TableHead>Approval Process</TableHead>
                            <TableHead>Action</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {requests.map((request) => (
                            <TableRow key={request.id}>
                                <TableCell className="font-medium">{request.id}</TableCell>
                                <TableCell><Badge variant={request.status}>{request.status}</Badge></TableCell>
                                <TableCell>{new Date(request.createdDate || '').toLocaleString()}</TableCell>
                                <TableCell>{request.user?.keycloakId}</TableCell>
                                <TableCell>
                                    <Select 
                                        onValueChange={(value) => handleApprovalProcessChange(request.id!, value)}
                                        value={selectedApprovalProcesses[request.id!]?.toString() || ''}
                                    >
                                        <SelectTrigger className="w-full">
                                            <SelectValue placeholder="Select Approval Process" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {approvalProcesses.map((process) => (
                                                <SelectItem key={process.id} value={process.id!.toString()}>
                                                    {process.name}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </TableCell>
                                <TableCell>
                                    <Button
                                        onClick={() => assignApprovalProcess(request.id!)}
                                        disabled={!selectedApprovalProcesses[request.id!]}
                                    >
                                        Assign
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

export default RequestsWithoutApprovalProcess;

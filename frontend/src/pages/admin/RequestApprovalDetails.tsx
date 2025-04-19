import { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams, useNavigate } from 'react-router-dom';
import { IApprovalProcess, IApprovalStep, IApprovalReview } from "@/bemodel/ApprovalProcess.ts";
import { IUser, IRequest } from "@/bemodel/Api.ts";
import { Button } from "@/components/ui/button.tsx";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import { Badge } from "@/components/ui/badge.tsx";
import { toast } from "sonner";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { ArrowLeft, X } from "lucide-react";

function RequestApprovalDetails() {
    const { requestId } = useParams<{ requestId: string }>();
    const navigate = useNavigate();

    const [loading, setLoading] = useState<boolean>(true);
    const [request, setRequest] = useState<IRequest | null>(null);
    const [approvalProcess, setApprovalProcess] = useState<IApprovalProcess | null>(null);
    const [steps, setSteps] = useState<IApprovalStep[]>([]);
    const [availableUsers, setAvailableUsers] = useState<IUser[]>([]);
    const [selectedReviewers, setSelectedReviewers] = useState<Record<number, number>>({});

    useEffect(() => {
        if (requestId) {
            fetchRequestDetails();
            fetchAvailableUsers();
        }
    }, [requestId]);

    const fetchRequestDetails = async () => {
        setLoading(true);
        try {
            // Fetch request details
            const requestResponse = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests/${requestId}`, {
                headers: { 'Content-Type': 'application/json' },
            });

            setRequest(requestResponse.data);

            if (requestResponse.data.approvalProcess?.id) {
                // Fetch approval process details
                const processId = requestResponse.data.approvalProcess.id;
                const processResponse = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-processes/${processId}`, {
                    headers: { 'Content-Type': 'application/json' },
                });

                setApprovalProcess(processResponse.data);

                // Fetch steps for this approval process
                const stepsResponse = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-steps/process/${processId}`, {
                    headers: { 'Content-Type': 'application/json' },
                });

                // Sort steps by order
                const sortedSteps = stepsResponse.data.sort((a: IApprovalStep, b: IApprovalStep) => 
                    (a.stepOrder || 0) - (b.stepOrder || 0)
                );

                setSteps(sortedSteps);

                // For each step, fetch its reviews
                for (const step of sortedSteps) {
                    if (step.id) {
                        const reviewsResponse = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-reviews/step/${step.id}`, {
                            headers: { 'Content-Type': 'application/json' },
                        });

                        // Update the step with its reviews
                        step.reviews = reviewsResponse.data;
                    }
                }
            }
        } catch (error) {
            console.error('Error fetching request details:', error);
            toast.error('Failed to fetch request details');
        } finally {
            setLoading(false);
        }
    };

    const fetchAvailableUsers = async () => {
        try {
            const response = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-steps/random-reviewers?count=100`, {
                headers: { 'Content-Type': 'application/json' },
            });

            setAvailableUsers(response.data);
        } catch (error) {
            console.error('Error fetching available users:', error);
            toast.error('Failed to fetch available users');
        }
    };

    const suggestReviewers = async (stepId: number) => {
        try {
            const response = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/approval-steps/${stepId}/suggest-reviewers`, {
                headers: { 'Content-Type': 'application/json' },
            });

            // Update the available users with the suggested reviewers
            setAvailableUsers(response.data);

            // If there are suggested reviewers, select the first one
            if (response.data.length > 0 && response.data[0].id !== null && response.data[0].id !== undefined) {
                handleReviewerChange(stepId, response.data[0].id.toString());
            }

            toast.success(`Suggested ${response.data.length} reviewers for this step`);
        } catch (error) {
            console.error('Error suggesting reviewers:', error);
            toast.error('Failed to suggest reviewers');
        }
    };

    const handleReviewerChange = (stepId: number, reviewerId: string) => {
        // Only set the reviewer if it's a valid numeric ID (not a placeholder)
        if (reviewerId !== 'placeholder') {
            setSelectedReviewers({
                ...selectedReviewers,
                [stepId]: parseInt(reviewerId)
            });
        }
    };

    const assignReviewer = async (stepId: number) => {
        const reviewerId = selectedReviewers[stepId];
        if (!reviewerId) {
            toast.error('Please select a reviewer');
            return;
        }

        try {
            // Create a new review with the selected reviewer
            await axios.post(`${import.meta.env.VITE_BACKEND_URL}/api/approval-reviews/step/${stepId}/reviewer/${reviewerId}`, {
                comment: "Assigned by admin",
                approved: null
            }, {
                headers: { 'Content-Type': 'application/json' },
            });

            toast.success('Reviewer assigned successfully');
            fetchRequestDetails(); // Refresh the data
        } catch (error) {
            console.error('Error assigning reviewer:', error);
            toast.error('Failed to assign reviewer');
        }
    };

    const removeReviewer = async (reviewId: number) => {
        try {
            await axios.delete(`${import.meta.env.VITE_BACKEND_URL}/api/approval-reviews/${reviewId}`, {
                headers: { 'Content-Type': 'application/json' },
            });

            toast.success('Reviewer removed successfully');
            fetchRequestDetails(); // Refresh the data
        } catch (error) {
            console.error('Error removing reviewer:', error);
            toast.error('Failed to remove reviewer');
        }
    };

    const goBack = () => {
        navigate('/admin/requests-with-approval-process');
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    if (!request || !request.approvalProcess) {
        return (
            <div>
                <Button onClick={goBack} variant="outline" className="mb-4">
                    <ArrowLeft className="mr-2 h-4 w-4" /> Back to Requests
                </Button>
                <div>Request not found or has no approval process.</div>
            </div>
        );
    }

    return (
        <div>
            <Button onClick={goBack} variant="outline" className="mb-4">
                <ArrowLeft className="mr-2 h-4 w-4" /> Back to Requests
            </Button>

            <h1 className="text-2xl font-bold mb-4">Request Approval Details</h1>

            <Card className="mb-6">
                <CardHeader>
                    <CardTitle>Request Information</CardTitle>
                    <CardDescription>Basic information about the request</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <p className="text-sm font-medium">Request ID</p>
                            <p>{request.id}</p>
                        </div>
                        <div>
                            <p className="text-sm font-medium">Status</p>
                            <Badge variant={request.status}>{request.status}</Badge>
                        </div>
                        <div>
                            <p className="text-sm font-medium">Created Date</p>
                            <p>{new Date(request.createdDate || '').toLocaleString()}</p>
                        </div>
                        <div>
                            <p className="text-sm font-medium">User</p>
                            <p>{request.user?.name || request.user?.keycloakId || 'N/A'}</p>
                        </div>
                    </div>
                </CardContent>
            </Card>

            <Card className="mb-6">
                <CardHeader>
                    <CardTitle>Approval Process</CardTitle>
                    <CardDescription>{approvalProcess?.name}</CardDescription>
                </CardHeader>
                <CardContent>
                    <p className="mb-4">{approvalProcess?.description}</p>

                    <h3 className="text-lg font-medium mb-4">Steps</h3>

                    {steps.map((step, index) => (
                        <div key={step.id || `step-${step.stepOrder || Math.random()}`} className="mb-6">
                            <div className="flex justify-between items-center mb-2">
                                <h4 className="text-md font-medium">
                                    Step {step.stepOrder}: {step.name}
                                </h4>
                                <Badge>{step.status}</Badge>
                            </div>
                            <p className="text-sm text-gray-500 mb-2">{step.description}</p>
                            <p className="text-sm mb-2">Minimum Reviewers: {step.minReviewers}</p>
                            <p className="text-sm mb-2">Requires Approval: {step.requiresApproval ? 'Yes' : 'No'}</p>

                            <div className="mt-4">
                                <h5 className="text-sm font-medium mb-2">Assigned Reviewers</h5>

                                {step.reviews && step.reviews.length > 0 ? (
                                    <div className="flex flex-wrap gap-2 mb-2">
                                        {step.reviews.map((review: IApprovalReview) => {
                                            const reviewer = availableUsers.find(u => u.id === review.reviewerId);
                                            const reviewerName = reviewer?.name || reviewer?.keycloakId || 
                                                (review.reviewerId ? `User ID: ${review.reviewerId}` : 'Unknown User');

                                            return (
                                                <Badge 
                                                    key={review.id || `review-${Math.random()}`}
                                                    className="flex items-center gap-1 px-3 py-1"
                                                    variant="secondary"
                                                >
                                                    <span>{reviewerName}</span>
                                                    <button 
                                                        className="ml-1 rounded-full hover:bg-gray-200 p-1"
                                                        onClick={() => review.id && removeReviewer(review.id)}
                                                        title="Remove reviewer"
                                                    >
                                                        <X className="h-3 w-3" />
                                                    </button>
                                                </Badge>
                                            );
                                        })}
                                    </div>
                                ) : (
                                    <p className="text-sm text-gray-500">No reviewers assigned yet.</p>
                                )}

                                <div className="mt-4">
                                    <h5 className="text-sm font-medium mb-2">Available Reviewers</h5>
                                    <div className="flex flex-wrap gap-2 mb-4">
                                        {availableUsers.map((user) => {
                                            const userName = user.name || user.keycloakId || 'Unknown User';
                                            // Skip users that are already assigned as reviewers for this step
                                            const isAlreadyAssigned = step.reviews?.some(
                                                (review: IApprovalReview) => review.reviewerId === user.id
                                            );

                                            if (isAlreadyAssigned) return null;

                                            return (
                                                <Badge 
                                                    key={`${(user.name ? user.name.toLowerCase().replace(/\s+/g, '') : '')}${user.keycloakId || 'unknown'}`}
                                                    className="flex items-center gap-1 px-3 py-1 cursor-pointer hover:bg-primary hover:text-primary-foreground"
                                                    variant="outline"
                                                    onClick={() => {
                                                        if (step.id && user.id) {
                                                            handleReviewerChange(step.id, user.id.toString());
                                                            assignReviewer(step.id);
                                                        }
                                                    }}
                                                >
                                                    <span>{userName}</span>
                                                </Badge>
                                            );
                                        })}
                                    </div>
                                    <Button 
                                        variant="outline"
                                        onClick={() => step.id && suggestReviewers(step.id)}
                                        className="mt-2"
                                    >
                                        Suggest Reviewers
                                    </Button>
                                </div>
                            </div>

                            {index < steps.length - 1 && <Separator className="my-4" />}
                        </div>
                    ))}
                </CardContent>
            </Card>
        </div>
    );
}

export default RequestApprovalDetails;

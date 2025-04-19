import { useState } from 'react';
import axios from 'axios';
import { IApprovalProcess, IApprovalStep } from "@/bemodel/ApprovalProcess.ts";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Label } from "@/components/ui/label.tsx";
import { Textarea } from "@/components/ui/textarea.tsx";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import { Switch } from "@/components/ui/switch.tsx";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

function CreateApprovalProcess() {
    const navigate = useNavigate();
    const [name, setName] = useState<string>('');
    const [description, setDescription] = useState<string>('');
    const [steps, setSteps] = useState<Omit<IApprovalStep, 'id' | 'createdDate' | 'status' | 'reviews'>>([]);
    const [loading, setLoading] = useState<boolean>(false);

    // Form state for a new step
    const [stepName, setStepName] = useState<string>('');
    const [stepDescription, setStepDescription] = useState<string>('');
    const [minReviewers, setMinReviewers] = useState<number>(1);
    const [requiresApproval, setRequiresApproval] = useState<boolean>(true);

    const addStep = () => {
        if (!stepName) {
            toast.error('Step name is required');
            return;
        }

        if (minReviewers < 1) {
            toast.error('Minimum reviewers must be at least 1');
            return;
        }

        const newStep = {
            name: stepName,
            description: stepDescription,
            stepOrder: steps.length + 1,
            minReviewers,
            requiresApproval
        };

        setSteps([...steps, newStep]);
        
        // Reset form fields for the next step
        setStepName('');
        setStepDescription('');
        setMinReviewers(1);
        setRequiresApproval(true);
    };

    const removeStep = (index: number) => {
        const updatedSteps = [...steps];
        updatedSteps.splice(index, 1);
        
        // Update step orders
        const reorderedSteps = updatedSteps.map((step, idx) => ({
            ...step,
            stepOrder: idx + 1
        }));
        
        setSteps(reorderedSteps);
    };

    const handleSubmit = () => {
        if (!name) {
            toast.error('Process name is required');
            return;
        }

        if (steps.length === 0) {
            toast.error('At least one step is required');
            return;
        }

        setLoading(true);

        const approvalProcess: Omit<IApprovalProcess, 'id' | 'createdDate'> = {
            name,
            description,
            steps: steps as IApprovalStep[]
        };

        axios.post(`${import.meta.env.VITE_BACKEND_URL}/api/approval-processes`, approvalProcess, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then(() => {
                toast.success('Approval process created successfully');
                navigate('/admin/requests-without-approval-process');
            })
            .catch((error) => {
                console.error('Error:', error);
                toast.error('Failed to create approval process');
            })
            .finally(() => {
                setLoading(false);
            });
    };

    return (
        <div>
            <h1 className="text-2xl font-bold mb-4">Create Approval Process</h1>
            
            <Card className="mb-6">
                <CardHeader>
                    <CardTitle>Process Details</CardTitle>
                    <CardDescription>Enter the details of the approval process</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="grid gap-4">
                        <div className="grid gap-2">
                            <Label htmlFor="name">Name</Label>
                            <Input 
                                id="name" 
                                value={name} 
                                onChange={(e) => setName(e.target.value)} 
                                placeholder="Enter process name"
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="description">Description</Label>
                            <Textarea 
                                id="description" 
                                value={description} 
                                onChange={(e) => setDescription(e.target.value)} 
                                placeholder="Enter process description"
                            />
                        </div>
                    </div>
                </CardContent>
            </Card>

            <Card className="mb-6">
                <CardHeader>
                    <CardTitle>Steps</CardTitle>
                    <CardDescription>Add steps to the approval process</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="grid gap-4">
                        {steps.map((step, index) => (
                            <Card key={index} className="border border-gray-200">
                                <CardHeader className="pb-2">
                                    <div className="flex justify-between items-center">
                                        <CardTitle className="text-lg">{step.name}</CardTitle>
                                        <Button 
                                            variant="destructive" 
                                            size="sm" 
                                            onClick={() => removeStep(index)}
                                        >
                                            Remove
                                        </Button>
                                    </div>
                                </CardHeader>
                                <CardContent>
                                    <p className="text-sm text-gray-500 mb-2">Order: {step.stepOrder}</p>
                                    {step.description && <p className="text-sm mb-2">{step.description}</p>}
                                    <p className="text-sm">Min Reviewers: {step.minReviewers}</p>
                                    <p className="text-sm">Requires Approval: {step.requiresApproval ? 'Yes' : 'No'}</p>
                                </CardContent>
                            </Card>
                        ))}

                        <div className="border border-dashed border-gray-300 p-4 rounded-lg">
                            <h3 className="text-lg font-medium mb-2">Add New Step</h3>
                            <div className="grid gap-4">
                                <div className="grid gap-2">
                                    <Label htmlFor="stepName">Step Name</Label>
                                    <Input 
                                        id="stepName" 
                                        value={stepName} 
                                        onChange={(e) => setStepName(e.target.value)} 
                                        placeholder="Enter step name"
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="stepDescription">Step Description</Label>
                                    <Textarea 
                                        id="stepDescription" 
                                        value={stepDescription} 
                                        onChange={(e) => setStepDescription(e.target.value)} 
                                        placeholder="Enter step description"
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="minReviewers">Minimum Reviewers</Label>
                                    <Input 
                                        id="minReviewers" 
                                        type="number" 
                                        min={1}
                                        value={minReviewers} 
                                        onChange={(e) => setMinReviewers(parseInt(e.target.value))} 
                                    />
                                </div>
                                <div className="flex items-center gap-2">
                                    <Switch 
                                        id="requiresApproval" 
                                        checked={requiresApproval} 
                                        onCheckedChange={setRequiresApproval} 
                                    />
                                    <Label htmlFor="requiresApproval">Requires Approval</Label>
                                </div>
                                <Button onClick={addStep}>Add Step</Button>
                            </div>
                        </div>
                    </div>
                </CardContent>
                <CardFooter>
                    <Button 
                        className="w-full" 
                        onClick={handleSubmit} 
                        disabled={loading || !name || steps.length === 0}
                    >
                        {loading ? 'Creating...' : 'Create Approval Process'}
                    </Button>
                </CardFooter>
            </Card>
        </div>
    );
}

export default CreateApprovalProcess;

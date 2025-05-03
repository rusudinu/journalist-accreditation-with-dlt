import { useEffect, useState, useRef } from 'react';
import {
    FileUploader,
    FileUploaderContent,
    FileUploaderItem,
    FileInput,
} from "@/components/extension/file-uploader";
import { DropzoneOptions } from "react-dropzone";
import { Button } from "@/components/ui/button.tsx";
import axios from 'axios';
import { toast } from "sonner";
import { useParams } from "react-router-dom";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { IoIosWarning } from "react-icons/io";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert.tsx";
import { IDocument } from "@/bemodel/Api.ts";
import { Eye } from 'lucide-react';
import { useAppSelector } from "@/hooks.ts";
import { Textarea } from "@/components/ui/textarea.tsx";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import { Progress } from "@/components/ui/progress.tsx";
import {IoCheckmark} from "react-icons/io5";

const FileSvgDraw = () => {
    return (
        <>
            <svg
                className="w-8 h-8 mb-3 text-gray-500 dark:text-gray-400"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 20 16"
            >
                <path
                    stroke="currentColor"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M13 13h3a3 3 0 0 0 0-6h-.025A5.56 5.56 0 0 0 16 6.5 5.5 5.5 0 0 0 5.207 5.021C5.137 5.017 5.071 5 5 5a4 4 0 0 0 0 8h2.167M10 15V6m0 0L8 8m2-2 2 2"
                />
            </svg>
            <p className="mb-1 text-sm text-gray-500 dark:text-gray-400">
                <span className="font-semibold">Click to upload</span>
                or drag and drop
            </p>
            <p className="text-xs text-gray-500 dark:text-gray-400">
                A document in PDF format
            </p>
        </>
    );
};

function RequestPage() {
    const { requestId: documentIdParam } = useParams<{ requestId: string }>();
    const [files, setFiles] = useState<File[] | null>([]);
    const [document, setDocument] = useState<IDocument | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [comment, setComment] = useState<string>("");
    const [isSubmittingComment, setIsSubmittingComment] = useState<boolean>(false);
    const [isDocumentValid, setIsDocumentValid] = useState<boolean | null>(null);
    const [isDocumentUploadValid, setIsDocumentUploadValid] = useState<boolean | null>(null);
    const [timeRemaining, setTimeRemaining] = useState<number | null>(null);
    const [isStartingPlenarySession, setIsStartingPlenarySession] = useState<boolean>(false);
    const timerIntervalRef = useRef<number | null>(null);
    const backendUrl = import.meta.env.VITE_BACKEND_URL;

    // Get the authenticated user's name from Redux store
    const authenticatedUserName = useAppSelector((state) => state.core.authenticatedUserName);

    // Check if user is allowed to upload files (specialtycommission or proposer)
    const isAllowedToUpload = () => {
        const normalizedUsername = authenticatedUserName.trim().toLowerCase().replace(/\s+/g, '');
        return normalizedUsername === 'specialtycommission' || normalizedUsername === 'legislativeproposer';
    };

    // Check if user is chamber president
    const isChamberPresident = () => {
        const normalizedUsername = authenticatedUserName.trim().toLowerCase().replace(/\s+/g, '');
        return normalizedUsername === 'chamberprezident';
    };

    useEffect(() => {
        fetchDocument();
    }, [documentIdParam]); // Dependency on the param from URL

    // Effect to update the countdown timer every second
    useEffect(() => {
        // Only start the timer if we have a document with a debate start date
        // and the user is chamber president and countdownAutoApproval is false
        if (document && 
            document.debateAndApprovalStartDate && 
            isChamberPresident() && 
            document.countdownAutoApproval === true) {

            // Calculate initial time remaining (15 minutes from start date)
            const startDate = new Date(document.debateAndApprovalStartDate);
            const endTime = new Date(startDate.getTime() + 15 * 60 * 1000); // 15 minutes in milliseconds

            // Update timer immediately
            updateTimeRemaining(endTime);

            // Set up interval to update timer every second
            const intervalId = window.setInterval(() => {
                updateTimeRemaining(endTime);
            }, 1000);

            // Store interval ID in ref
            timerIntervalRef.current = intervalId;

            // Clean up interval on unmount or when document changes
            return () => {
                if (timerIntervalRef.current !== null) {
                    window.clearInterval(timerIntervalRef.current);
                    timerIntervalRef.current = null;
                }
            };
        } else {
            // Clear any existing timer if conditions are not met
            if (timerIntervalRef.current !== null) {
                window.clearInterval(timerIntervalRef.current);
                timerIntervalRef.current = null;
            }

            // Reset time remaining if no timer should be active
            if (!document || 
                !document.debateAndApprovalStartDate || 
                !isChamberPresident() || 
                document.countdownAutoApproval === false) {
                setTimeRemaining(null);
            }
        }
    }, [document, isChamberPresident]);

    // Function to update the time remaining
    const updateTimeRemaining = (endTime: Date) => {
        const now = new Date();
        const diff = endTime.getTime() - now.getTime();
    
        if (diff <= 0) {
            // Timer has expired
            setTimeRemaining(0);

            // Clear the interval
            if (timerIntervalRef.current !== null) {
                window.clearInterval(timerIntervalRef.current);
                timerIntervalRef.current = null;
            }
        } else {
            // Update time remaining in seconds
            setTimeRemaining(Math.floor(diff / 1000));
        }
    };

    // Function to handle starting the plenary session
    const handleStartPlenarySession = async () => {
        if (!document?.id) {
            toast('Error', {
                description: 'Document ID is missing.',
            });
            return;
        }

        setIsStartingPlenarySession(true);

        try {
            const response = await axios.post(
                `${backendUrl}/api/v1/documents/start-plenary-session/${document.id}`
            );

            if (response.status === 200) {
                toast('Success', {
                    description: 'Plenary session started successfully!',
                });
                fetchDocument(); // Refresh document data
            } else {
                toast('Error', {
                    description: `Failed to start plenary session. Server responded with status: ${response.status}`,
                });
            }
        } catch (error: unknown) {
            console.error("Error starting plenary session:", error);
            toast('Error', {
                description: `An error occurred while starting the plenary session: ${error instanceof Error ? error.message : 'Unknown error'}`,
            });
        } finally {
            setIsStartingPlenarySession(false);
        }
    };

    const fetchDocument = () => {
        if (documentIdParam) {
            setIsLoading(true); // Set loading true before fetch

            // Fetch document details
            axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/${documentIdParam}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
            })
                .then((response) => {
                    setDocument(response.data);

                    // After getting document, fetch document validity
                    axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/document-valid/${documentIdParam}`)
                        .then((validityResponse) => {
                            setIsDocumentValid(validityResponse.data);
                        })
                        .catch((validityError) => {
                            console.error('Error fetching document validity:', validityError);
                            setIsDocumentValid(null);
                        });

                    // Fetch document upload validity
                    axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/document-doc-valid/${documentIdParam}`)
                        .then((uploadValidityResponse) => {
                            setIsDocumentUploadValid(uploadValidityResponse.data);
                        })
                        .catch((uploadValidityError) => {
                            console.error('Error fetching document upload validity:', uploadValidityError);
                            setIsDocumentUploadValid(null);
                        });
                })
                .catch((error: unknown) => {
                    console.error('Error fetching document:', error);
                    toast('Error', {
                        description: `Failed to fetch document details: ${error instanceof Error ? error.message : 'Unknown error'}`,
                    });
                    // Consider adding retry logic or error state handling here
                    // Example retry:
                    // setTimeout(() => {
                    //     fetchDocument();
                    // }, 5000); // Retry after 5 seconds
                })
                .finally(() => {
                    setIsLoading(false); // Set loading false after fetch attempt
                });
        } else {
            setIsLoading(false); // No ID, stop loading
            console.error("Document ID parameter is missing.");
            toast('Error', { description: 'Document ID is missing in the URL.' });
        }
    }

    // Removed: checkIfRequestIsValid function

    const dropzone = {
        accept: {
            "application/pdf": [".pdf"],
        },
        multiple: false, // Keep as false if only one file should replace the existing one
        maxSize: 5 * 1024 * 1024, // 5MB
    } satisfies DropzoneOptions;

    const handleUpload = async () => {
        if (!files || files.length === 0) {
            toast('No file selected', {
                description: 'Please select a file to upload.',
            });
            return;
        }
        if (!document?.id) {
            toast('Error', {
                description: 'Cannot upload file: Document ID is missing.',
            });
            return;
        }

        const formData = new FormData();
        // Since multiple: false, we expect only one file
        formData.append("file", files[0]);

        try {
            // Determine which endpoint to use based on user role
            const normalizedUsername = authenticatedUserName.trim().toLowerCase().replace(/\s+/g, '');
            let endpoint = '/api/v1/documents';

            // If user is specialtycommission, use the specialty-commission endpoint
            if (normalizedUsername === 'specialtycommission') {
                endpoint = '/api/v1/documents/specialty-commission';
            }

            // The query parameter identifies which document record to associate the file with
            const response = await axios.post(`${import.meta.env.VITE_BACKEND_URL}${endpoint}?documentId=${document.id}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            if (response.status === 200) {
                toast('Success', {
                    description: 'File uploaded successfully!',
                });
                setFiles([]); // Clear the selection
                fetchDocument(); // Refresh document data to show the new storedDocumentName etc.
            } else {
                toast('Upload failed', {
                    description: `Failed to upload file. Server responded with status: ${response.status}`,
                });
            }
        } catch (error: unknown) {
            console.error("Error uploading file:", error);
            toast('Error uploading file', {
                description: `An error occurred while uploading the file: ${error instanceof Error ? error.message : 'Unknown error'}`,
            });
        }
    };

    // Determine if the document is considered 'readonly' based on its properties
    const isReadOnly = document?.deleted === true; // Example condition, adjust as needed

    if (isLoading) {
        return <div>Loading document details...</div>;
    }

    if (!document) {
        return <div className="text-red-600">Failed to load document details. Please try again later.</div>;
    }

    const handlePreview = () => {
        if (document?.storedDocumentName && backendUrl) {
            const previewUrl = `${backendUrl}/api/v1/documents/download/${document.storedDocumentName}`;
            window.open(previewUrl, '_blank', 'noopener,noreferrer'); // Added rel for security
        } else {
            toast('Error', {
                description: 'Cannot preview file: Stored document name or backend URL is missing.',
            });
        }
    };

    const handleSpecialtyCommissionPreview = () => {
        if (document?.decidingSpecialtyCommissionDocumentName && backendUrl) {
            const previewUrl = `${backendUrl}/api/v1/documents/download/${document.decidingSpecialtyCommissionDocumentName}`;
            window.open(previewUrl, '_blank', 'noopener,noreferrer'); // Added rel for security
        } else {
            toast('Error', {
                description: 'Cannot preview file: Specialty Commission document name or backend URL is missing.',
            });
        }
    };

    // Function to submit a comment based on user role
    const submitComment = async () => {
        if (!comment.trim()) {
            toast('Error', {
                description: 'Please enter a comment before submitting.',
            });
            return;
        }

        if (!document?.id) {
            toast('Error', {
                description: 'Document ID is missing.',
            });
            return;
        }

        setIsSubmittingComment(true);

        try {
            // Normalize the username to determine which endpoint to use
            const normalizedUsername = authenticatedUserName.trim().toLowerCase().replace(/\s+/g, '');
            let endpoint = '';

            switch (normalizedUsername) {
                case 'economicandsocialcouncil':
                    endpoint = 'economic-and-social';
                    break;
                case 'generalsecretariat':
                    endpoint = 'general-secretariat';
                    break;
                case 'legislativecouncil':
                    endpoint = 'legislative-council';
                    break;
                case 'legalcommittee':
                    endpoint = 'legal-committee';
                    break;
                case 'budgetcommittee':
                    endpoint = 'budget-committee';
                    break;
                case 'publicadministration':
                    endpoint = 'public-administration';
                    break;
                case 'specialtycommission':
                    endpoint = 'specialty-commission';
                    break;
                default:
                    toast('Error', {
                        description: 'You are not authorized to add comments.',
                    });
                    setIsSubmittingComment(false);
                    return;
            }

            const response = await axios.post(
                `${backendUrl}/api/v1/documents/${endpoint}/${document.id}`,
                null,
                {
                    params: {
                        comment: comment
                    }
                }
            );

            if (response.status === 200) {
                toast('Success', {
                    description: 'Comment added successfully!',
                });
                setComment('');
                fetchDocument(); // Refresh document data to show the new comment
            } else {
                toast('Error', {
                    description: `Failed to add comment. Server responded with status: ${response.status}`,
                });
            }
        } catch (error: unknown) {
            console.error("Error adding comment:", error);
            toast('Error', {
                description: `An error occurred while adding the comment: ${error instanceof Error ? error.message : 'Unknown error'}`,
            });
        } finally {
            setIsSubmittingComment(false);
        }
    };

    return (
        <>
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[100px]">Document ID</TableHead>
                        <TableHead>Uploaded Filename</TableHead>
                        <TableHead>Created Date</TableHead>
                        <TableHead>Preview</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    <TableRow key={document.id}>
                        <TableCell className="font-medium">{document.id}</TableCell>
                        <TableCell>
                            {document.storedDocumentName
                                ? document.storedDocumentName
                                : <span className="text-gray-500 italic">No file uploaded yet</span>
                            }
                        </TableCell>
                        <TableCell>
                            {document.createdDate
                                ? new Date(document.createdDate).toLocaleString()
                                : 'N/A'
                            }
                        </TableCell>
                        <TableCell className="text-center">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={handlePreview}
                                disabled={!document.storedDocumentName} // Disable if no stored name
                                title="Preview Document" // Add tooltip
                            >
                                <Eye className="h-4 w-4 mr-1" />
                                Preview
                            </Button>
                        </TableCell>
                    </TableRow>
                    {document.decidingSpecialtyCommissionDocumentName && (
                        <TableRow key={`${document.id}-specialty`}>
                            <TableCell className="font-medium">{document.id}</TableCell>
                            <TableCell>
                                <span className="font-semibold text-blue-600">Specialty Commission: </span>
                                {document.decidingSpecialtyCommissionDocumentName}
                            </TableCell>
                            <TableCell>
                                {document.debateAndApprovalStartDate
                                    ? new Date(document.debateAndApprovalStartDate).toLocaleString()
                                    : 'N/A'
                                }
                            </TableCell>
                            <TableCell className="text-center">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handleSpecialtyCommissionPreview}
                                    title="Preview Specialty Commission Document"
                                >
                                    <Eye className="h-4 w-4 mr-1" />
                                    Preview
                                </Button>
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>

            <Separator className="my-4"/>

            {/* Document Comments Validity Status */}
            {isDocumentValid !== null && (
                <Alert className="mb-4" variant={isDocumentValid ? "default" : "destructive"}>
                    {isDocumentValid ? <IoCheckmark className="h-4 w-4"/> : <IoIosWarning className="h-4 w-4"/>}
                    <AlertTitle>{isDocumentValid ? "Document Comments Valid" : "Document Comments Invalid"}</AlertTitle>
                    <AlertDescription>
                        {isDocumentValid 
                            ? "All document comment hashes are valid. The comments have not been tampered with."
                            : "Some document comment hashes are invalid. The comments may have been tampered with."
                        }
                    </AlertDescription>
                </Alert>
            )}

            {/* Document Upload Validity Status */}
            {isDocumentUploadValid !== null && (
                <Alert className="mb-4" variant={isDocumentUploadValid ? "default" : "destructive"}>
                    {isDocumentUploadValid ? <IoCheckmark className="h-4 w-4"/> : <IoIosWarning className="h-4 w-4"/>}
                    <AlertTitle>{isDocumentUploadValid ? "Document Upload Valid" : "Document Upload Invalid"}</AlertTitle>
                    <AlertDescription>
                        {isDocumentUploadValid 
                            ? "The document upload hash is valid. The uploaded file has not been tampered with."
                            : "The document upload hash is invalid. The uploaded file may have been tampered with."
                        }
                    </AlertDescription>
                </Alert>
            )}

            { isReadOnly &&
                <Alert className="mb-4" variant="destructive">
                    <IoIosWarning className="h-4 w-4"/>
                    <AlertTitle>Document Archived</AlertTitle>
                    <AlertDescription>
                        This document is marked as deleted and cannot be modified or receive new file uploads.
                    </AlertDescription>
                </Alert>
            }

            { !isReadOnly && isAllowedToUpload() && (
                <>
                    <FileUploader
                        value={files}
                        onValueChange={setFiles}
                        dropzoneOptions={dropzone}
                    >
                        <FileInput className={`border bg-background rounded-md ${isReadOnly ? 'cursor-not-allowed opacity-50' : ''}`}>
                            <div className="flex items-center justify-center flex-col pt-3 pb-4 w-full">
                                <FileSvgDraw/>
                            </div>
                        </FileInput>
                        <FileUploaderContent className="flex items-center flex-row gap-2 mt-2">
                            {files?.map((_file, i) => (
                                <FileUploaderItem
                                    key={i}
                                    index={i}
                                />
                            ))}
                        </FileUploaderContent>
                    </FileUploader>
                    <div className="mt-4 flex justify-between items-center">
                        <Button
                            onClick={handleUpload}
                            disabled={!files || files.length === 0 || isReadOnly}
                        >
                            Upload File
                        </Button>
                    </div>
                </>
            )}

            {/* Comment Section */}
            <Separator className="my-4"/>

            {!isReadOnly && document && (
                <>
                    {/* Display existing comments if they exist */}
                    <div className="mb-4">
                        <h3 className="text-lg font-semibold mb-2">Comments</h3>

                        {document.economicAndSocialCouncilComment && (
                            <Card className="mb-2">
                                <CardHeader>
                                    <CardTitle>Economic and Social Council</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p>{document.economicAndSocialCouncilComment}</p>
                                </CardContent>
                            </Card>
                        )}

                        {document.generalSecretariatComment && (
                            <Card className="mb-2">
                                <CardHeader>
                                    <CardTitle>General Secretariat</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p>{document.generalSecretariatComment}</p>
                                </CardContent>
                            </Card>
                        )}

                        {document.legislativeCouncilComment && (
                            <Card className="mb-2">
                                <CardHeader>
                                    <CardTitle>Legislative Council</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p>{document.legislativeCouncilComment}</p>
                                </CardContent>
                            </Card>
                        )}

                        {document.legalCommitteeComment && (
                            <Card className="mb-2">
                                <CardHeader>
                                    <CardTitle>Legal Committee</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p>{document.legalCommitteeComment}</p>
                                </CardContent>
                            </Card>
                        )}

                        {document.budgetCommitteeComment && (
                            <Card className="mb-2">
                                <CardHeader>
                                    <CardTitle>Budget Committee</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p>{document.budgetCommitteeComment}</p>
                                </CardContent>
                            </Card>
                        )}

                        {document.publicAdministrationComment && (
                            <Card className="mb-2">
                                <CardHeader>
                                    <CardTitle>Public Administration</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p>{document.publicAdministrationComment}</p>
                                </CardContent>
                            </Card>
                        )}

                        {document.specialtyCommissionComment && (
                            <Card className="mb-2">
                                <CardHeader>
                                    <CardTitle>Specialty Commission</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <p>{document.specialtyCommissionComment}</p>
                                </CardContent>
                            </Card>
                        )}
                    </div>

                    {/* Add Comment Form based on user role */}
                    {(() => {
                        // Normalize the username to determine which comment form to show
                        const normalizedUsername = authenticatedUserName.trim().toLowerCase().replace(/\s+/g, '');
                        let commentTitle = '';
                        let existingComment = '';

                        switch (normalizedUsername) {
                            case 'economicandsocialcouncil':
                                commentTitle = 'Economic and Social Council Comment';
                                existingComment = document.economicAndSocialCouncilComment || '';
                                break;
                            case 'generalsecretariat':
                                commentTitle = 'General Secretariat Comment';
                                existingComment = document.generalSecretariatComment || '';
                                break;
                            case 'legislativecouncil':
                                commentTitle = 'Legislative Council Comment';
                                existingComment = document.legislativeCouncilComment || '';
                                break;
                            case 'legalcommittee':
                                commentTitle = 'Legal Committee Comment';
                                existingComment = document.legalCommitteeComment || '';
                                break;
                            case 'budgetcommittee':
                                commentTitle = 'Budget Committee Comment';
                                existingComment = document.budgetCommitteeComment || '';
                                break;
                            case 'publicadministration':
                                commentTitle = 'Public Administration Comment';
                                existingComment = document.publicAdministrationComment || '';
                                break;
                            case 'specialtycommission':
                                commentTitle = 'Specialty Commission Comment';
                                existingComment = document.specialtyCommissionComment || '';
                                break;
                            default:
                                return null; // No comment form for other users
                        }

                        // If the user has already added a comment, show it and don't allow adding another
                        if (existingComment) {
                            return (
                                <Alert className="mb-4">
                                    <AlertTitle>Comment Already Added</AlertTitle>
                                    <AlertDescription>
                                        You have already added a comment to this document.
                                    </AlertDescription>
                                </Alert>
                            );
                        }

                        if (isAllowedToUpload()) {
                            return (
                                <Alert className="mb-4">
                                    <AlertTitle>Comment Not Allowed</AlertTitle>
                                    <AlertDescription>
                                        You are not allowed to add comments to this document.
                                    </AlertDescription>
                                </Alert>
                            );
                        }

                        // Otherwise, show the comment form
                        return (
                            <Card>
                                <CardHeader>
                                    <CardTitle>{commentTitle}</CardTitle>
                                    <CardDescription>Add your comment to this document</CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <Textarea
                                        placeholder="Enter your comment here..."
                                        value={comment}
                                        onChange={(e) => setComment(e.target.value)}
                                        className="mb-4"
                                    />
                                    <Button 
                                        onClick={submitComment}
                                        disabled={isSubmittingComment || !comment.trim()}
                                    >
                                        {isSubmittingComment ? 'Submitting...' : 'Submit Comment'}
                                    </Button>
                                </CardContent>
                            </Card>
                        );
                    })()}
                </>
            )}

            {/* Chamber President Section */}
            {!isReadOnly && document && isChamberPresident() && document.decidingSpecialtyCommissionDocumentName && (
                <>
                    <Separator className="my-4"/>
                    <div className="mb-4">
                        <h3 className="text-lg font-semibold mb-2">Plenary Session</h3>

                        {document.countdownAutoApproval === true ? (
                            <Card>
                                <CardHeader>
                                    <CardTitle>Plenary Debate Countdown</CardTitle>
                                    <CardDescription>
                                        Time remaining before automatic approval
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    {timeRemaining !== null && (
                                        <>
                                            <div className="mb-2">
                                                {timeRemaining > 0 ? (
                                                    <>
                                                        <p className="text-center text-2xl font-bold mb-2">
                                                            {Math.floor(timeRemaining / 60)}:{(timeRemaining % 60).toString().padStart(2, '0')}
                                                        </p>
                                                        <Progress 
                                                            value={(timeRemaining / (15 * 60)) * 100} 
                                                            className="h-2 mb-4"
                                                        />
                                                    </>
                                                ) : (
                                                    <Alert className="mb-4">
                                                        <AlertTitle>Countdown Complete</AlertTitle>
                                                        <AlertDescription>
                                                            The 15-minute countdown has expired. The document has been automatically approved.
                                                        </AlertDescription>
                                                    </Alert>
                                                )}
                                            </div>
                                            <Button 
                                                onClick={handleStartPlenarySession}
                                                disabled={isStartingPlenarySession || timeRemaining <= 0}
                                                className="w-full"
                                            >
                                                {isStartingPlenarySession ? 'Starting...' : 'Start Plenary Debate'}
                                            </Button>
                                        </>
                                    )}
                                </CardContent>
                            </Card>
                        ) : (
                            <Card>
                                <CardHeader>
                                    <CardTitle>Plenary Debate Started</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <Alert className="mb-4">
                                        <AlertTitle>Plenary Debate in Progress</AlertTitle>
                                        <AlertDescription>
                                            The plenary debate has been started for this document.
                                        </AlertDescription>
                                    </Alert>

                                    {document.debateAndApprovalPlenarySessionVoteResults && 
                                     document.debateAndApprovalPlenarySessionVoteResults.length > 0 ? (
                                        <div className="mt-4">
                                            <h4 className="font-semibold mb-2">Vote Results:</h4>
                                            <div className="flex gap-4">
                                                <div className="bg-green-100 p-3 rounded-md flex-1 text-center">
                                                    <p className="font-bold text-green-800">YES</p>
                                                    <p className="text-xl">
                                                        {document.debateAndApprovalPlenarySessionVoteResults.filter(vote => vote === 'YES').length}
                                                    </p>
                                                </div>
                                                <div className="bg-red-100 p-3 rounded-md flex-1 text-center">
                                                    <p className="font-bold text-red-800">NO</p>
                                                    <p className="text-xl">
                                                        {document.debateAndApprovalPlenarySessionVoteResults.filter(vote => vote === 'NO').length}
                                                    </p>
                                                </div>
                                                <div className="bg-gray-100 p-3 rounded-md flex-1 text-center">
                                                    <p className="font-bold text-gray-800">ABSTAIN</p>
                                                    <p className="text-xl">
                                                        {document.debateAndApprovalPlenarySessionVoteResults.filter(vote => vote === 'ABSTAIN').length}
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    ) : (
                                        <p className="text-gray-500 italic">No votes have been cast yet.</p>
                                    )}
                                </CardContent>
                            </Card>
                        )}
                    </div>
                </>
            )}
        </>
    );
}

export default RequestPage;

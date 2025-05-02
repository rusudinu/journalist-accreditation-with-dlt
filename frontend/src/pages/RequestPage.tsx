import {useEffect, useState} from 'react';
import {
    FileUploader,
    FileUploaderContent,
    FileUploaderItem,
    FileInput,
} from "@/components/extension/file-uploader";
import {DropzoneOptions} from "react-dropzone";
import {Button} from "@/components/ui/button.tsx";
import axios from 'axios';
import {toast} from "sonner";
import {useParams} from "react-router-dom";
import {IRequest} from "@/bemodel/Api.ts";
import RequestsDocumentTable from "@/pages/RequestsDocumentTable.tsx";
import {ComboboxPopover, Status} from "@/components/extension/Combobox.tsx";
import {useUserHasRole} from "@/common/auth/UserUtils.ts";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {Separator} from "@/components/ui/separator.tsx";
import {IoIosWarning} from "react-icons/io";
import {Alert, AlertDescription, AlertTitle} from "@/components/ui/alert.tsx";

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
                &nbsp; or drag and drop
            </p>
            <p className="text-xs text-gray-500 dark:text-gray-400">
                A document in PDF format
            </p>
        </>
    );
};

function RequestPage() {
    const {requestId} = useParams<{ requestId: string }>();
    const [files, setFiles] = useState<File[] | null>([]);
    const [request, setRequest] = useState<IRequest | null>(null);
    const [selectedStatus, setSelectedStatus] = useState<Status | null>(null);
    const [verifiedRequest, setVerifiedRequest] = useState<boolean | null>(null);
    const [selectedDocumentId, setSelectedDocumentId] = useState<number | null>(null);
    const isJournalist = useUserHasRole('JOURNALIST');
    const isJuridic = useUserHasRole('JURIDIC');
    const isDirector = useUserHasRole('DIRECTOR');

    const allStatuses: Status[] = [
        {value: "CREATED", label: "Created",},
        {value: "VALIDATED", label: "Validated",},
        {value: "APPROVED", label: "Approved",},
        {value: "REJECTED", label: "Rejected",},
    ];
    const journalistStatuses: Status[] = [
        {value: "CREATED", label: "Created",},
    ];

    const juridicStatuses: Status[] = [
        {value: "VALIDATED", label: "Validated",},
    ];

    const directorStatuses: Status[] = [
        {value: "APPROVED", label: "Approved",},
        {value: "REJECTED", label: "Rejected",},
    ];

    const [defaultStatus, setDefaultStatus] = useState<Status | null>(null);

    const [statuses, setStatuses] = useState<Status[]>([allStatuses[0]]);

    useEffect(() => {
        if (isJournalist) {
            setStatuses(journalistStatuses);
            setDefaultStatus(journalistStatuses[0]);
        } else if (isJuridic) {
            setStatuses(juridicStatuses);
            setDefaultStatus(juridicStatuses[0]);
        } else if (isDirector) {
            setStatuses(directorStatuses);
            setDefaultStatus(directorStatuses[0]);
        }

    }, [isJournalist, isJuridic, isDirector, journalistStatuses, juridicStatuses, directorStatuses]);

    useEffect(() => {
        fetchRequest();
    }, [requestId]);

    const fetchRequest = () => {
        if (requestId) {
            axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests/${requestId}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
            })
                .then((response) => {
                    setRequest(response.data);
                    // Set the selected document ID to the most recent document's ID if available
                    if (response.data.documents && response.data.documents.length > 0) {
                        const mostRecentDocument = response.data.documents[response.data.documents.length - 1];
                        setSelectedDocumentId(mostRecentDocument.id);
                    }
                    checkIfRequestIsValid();
                })
                .catch((error: unknown) => {
                    console.error('Error:', error);
                    setTimeout(() => {
                        fetchRequest();
                    }, 200);
                });
        }
    }

    const checkIfRequestIsValid = () => {
        if (requestId) {
            axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests/verify/${requestId}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
            })
                .then((response) => {
                    setVerifiedRequest(response.data);
                })
                .catch((error: unknown) => {
                    console.error('Error:', error);
                    setTimeout(() => {
                        fetchRequest();
                    }, 200);
                });
        }
    }

    const dropzone = {
        accept: {
            "application/pdf": [".pdf"],
        },
        multiple: false,
        maxSize: 5 * 1024 * 1024, // 5MB
    } satisfies DropzoneOptions;

    const handleStatusChange = (status: Status | null) => {
        setSelectedStatus(status)
    }

    const handleUpload = async () => {
        if (!files || files.length === 0) {
            toast('No files selected', {
                description: 'Please select files to upload.',
            });
            return;
        }

        const formData = new FormData();
        files.forEach(file => {
            formData.append("file", file);
        });

        try {
            const response = await axios.post(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents?status=${selectedStatus?.value}&requestId=${requestId}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            if (response.status === 200) {
                toast('Success', {
                    description: 'Files uploaded successfully!',
                });
                setFiles([]);
                fetchRequest();
            } else {
                toast('Upload failed', {
                    description: 'Failed to upload files.',
                });
            }
        } catch (error: unknown) {
            console.error("Error uploading files:", error);
            toast('Error uploading files', {
                description: `An error occurred while uploading the files: ${error instanceof Error ? error.message : 'Unknown error'}`,
            });
        }
    };

    return (
        <>
            {
                verifiedRequest === null ? <div>Loading...</div> : <Badge>{verifiedRequest ? "Verified" : "Request or its documents were altered."}</Badge>
            }
            {
                request && <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[100px]">Request ID</TableHead>
                            <TableHead>Uploaded documents</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Created Date</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        <TableRow key={request.id}>
                            <TableCell className="font-medium">{request.id}</TableCell>
                            <TableCell>{request.documents?.length}</TableCell>
                            <TableCell><Badge>{request.status}</Badge></TableCell>
                            <TableCell>{request.createdDate}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            }
            <Separator className="my-4"/>

            {request && <div className="pb-6"><RequestsDocumentTable request={request}/></div>}

            {/* Comments section for the selected document */}
            {selectedDocumentId && request?.documents && request.documents.length > 0 && (
                <div className="mb-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-xl font-bold">Document Comments</h2>
                        <div className="flex items-center">
                            <span className="mr-2">Select Document:</span>
                            <select 
                                className="p-2 border rounded-md"
                                value={selectedDocumentId}
                                onChange={(e) => setSelectedDocumentId(Number(e.target.value))}
                            >
                                {request.documents.map((doc) => (
                                    <option key={doc.id} value={doc.id}>
                                        Document {doc.id} ({new Date(doc.createdDate || '').toLocaleDateString()})
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                    <Separator className="my-4"/>
                </div>
            )}

            {
                (request?.status === "APPROVED" || request?.status === "REJECTED") &&
                <Alert className="mb-2">
                    <IoIosWarning className="h-4 w-4" color="orange"/>
                    <AlertTitle>Readonly request</AlertTitle>
                    <AlertDescription>This request has been automatically archived and can no longer receive documents or status updates.</AlertDescription>
                </Alert>
            }
            {
                (request?.status === "CREATED" || request?.status === "VALIDATED") &&
                <>
                    <FileUploader
                        value={files}
                        onValueChange={setFiles}
                        dropzoneOptions={dropzone}
                    >
                        <FileInput className="border bg-background rounded-md">
                            <div className="flex items-center justify-center flex-col pt-3 pb-4 w-full">
                                <FileSvgDraw/>
                            </div>
                        </FileInput>
                        <FileUploaderContent className="flex items-center flex-row gap-2">
                            {files?.map((_file, i) => (
                                <FileUploaderItem
                                    key={i}
                                    index={i}
                                />
                            ))}
                        </FileUploaderContent>
                    </FileUploader>
                    <div className="mt-4 flex justify-between items-center">
                        <ComboboxPopover
                            statuses={statuses}
                            defaultStatus={defaultStatus}
                            onStatusChange={handleStatusChange}
                        />
                        <Button
                            onClick={handleUpload}
                            disabled={!files || files.length === 0 || !selectedStatus}
                        >
                            Upload
                        </Button>
                    </div>
                </>
            }
        </>
    );
}

export default RequestPage;

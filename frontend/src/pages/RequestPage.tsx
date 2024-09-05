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

    }, [isJournalist, isJuridic, isDirector]);

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
                })
                .catch((error) => {
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
        } catch (error: any) {
            console.error("Error uploading files:", error);
            toast('Error uploading files', {
                description: `An error occurred while uploading the files: ${error.message}`,
            });
        }
    };

    return (
        <>
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
                            <TableCell><Badge variant={request.status}>{request.status}</Badge></TableCell>
                            <TableCell>{request.createdDate}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            }
            <Separator className="my-4"/>
            {request && <div className="pb-12"><RequestsDocumentTable request={request}/></div>}
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
                            {files?.map((file, i) => {
                                const fileType = file.type;
                                const isImage = fileType.startsWith("image/");
                                const isPDF = fileType === "application/pdf";

                                return (
                                    <FileUploaderItem
                                        key={i}
                                        index={i}
                                        className="size-20 p-0 rounded-md overflow-hidden"
                                        aria-roledescription={`file ${i + 1} containing ${file.name}`}
                                    >
                                        {isImage ? (
                                            <img
                                                src={URL.createObjectURL(file)}
                                                alt={file.name}
                                                height={80}
                                                className="size-20 p-0"
                                            />
                                        ) : isPDF ? (
                                            <div className="flex items-center justify-center h-20 w-20 bg-gray-200 text-gray-700">
                                                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="size-6">
                                                    <path strokeLinecap="round" strokeLinejoin="round"
                                                          d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z"/>
                                                </svg>

                                            </div>
                                        ) : (
                                            <div className="flex items-center justify-center h-20 w-20 bg-gray-200 text-gray-700">
                                                <span>File</span>
                                            </div>
                                        )}
                                    </FileUploaderItem>
                                );
                            })}
                        </FileUploaderContent>
                    </FileUploader>
                    <div className="flex flex-row space-x-4 items-center content-center mt-4">
                        <Button
                            onClick={handleUpload}
                        >
                            Upload File with
                        </Button>
                        {
                            defaultStatus !== null && (
                                <ComboboxPopover statuses={statuses} defaultStatus={defaultStatus} onStatusChange={handleStatusChange}/>
                            )
                        }
                    </div>
                </>
            }
        </>
    );
}

export default RequestPage;

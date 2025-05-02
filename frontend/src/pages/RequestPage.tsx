import { useEffect, useState } from 'react';
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
    const backendUrl = import.meta.env.VITE_BACKEND_URL;

    useEffect(() => {
        fetchDocument();
    }, [documentIdParam]); // Dependency on the param from URL

    const fetchDocument = () => {
        if (documentIdParam) {
            setIsLoading(true); // Set loading true before fetch
            axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/${documentIdParam}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
            })
                .then((response) => {
                    setDocument(response.data);
                    // Removed: logic related to request.documents or selectedDocumentId
                    // Removed: checkIfRequestIsValid call
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
            // Assuming POST updates the document file or creates a new version
            // The query parameter identifies which document record to associate the file with
            const response = await axios.post(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents?documentId=${document.id}`, formData, {
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
                        <TableCell className="text-center"> {/* Added Actions Cell */}
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={handlePreview}
                                disabled={!document.storedDocumentName} // Disable if no stored name
                                title="Preview Document" // Add tooltip
                            >
                                <Eye className="h-4 w-4 mr-1" /> {/* Optional icon */}
                                Preview
                            </Button>
                        </TableCell>
                    </TableRow>
                </TableBody>
            </Table>

            <Separator className="my-4"/>

            { isReadOnly &&
                <Alert className="mb-4" variant="destructive">
                    <IoIosWarning className="h-4 w-4"/>
                    <AlertTitle>Document Archived</AlertTitle>
                    <AlertDescription>
                        This document is marked as deleted and cannot be modified or receive new file uploads.
                    </AlertDescription>
                </Alert>
            }

            { !isReadOnly && (
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
        </>
    );
}

export default RequestPage;

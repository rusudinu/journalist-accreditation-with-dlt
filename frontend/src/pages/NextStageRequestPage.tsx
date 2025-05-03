import { useEffect, useState } from 'react';
import { Button } from "@/components/ui/button.tsx";
import axios from 'axios';
import { toast } from "sonner";
import { useParams } from "react-router-dom";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table.tsx";
import { IDocument } from "@/bemodel/Api.ts";
import { Eye } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.tsx";

function NextStageRequestPage() {
    const { requestId: documentIdParam } = useParams<{ requestId: string }>();
    const [document, setDocument] = useState<IDocument | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const backendUrl = import.meta.env.VITE_BACKEND_URL;

    useEffect(() => {
        fetchDocument();
    }, [documentIdParam]); // Dependency on the param from URL

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
                })
                .catch((error: unknown) => {
                    console.error('Error fetching document:', error);
                    toast('Error', {
                        description: `Failed to fetch document details: ${error instanceof Error ? error.message : 'Unknown error'}`,
                    });
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

    const handleSpecialtyCommissionPreview = () => {
        if (document?.decidingSpecialtyCommissionDocumentName && backendUrl) {
            const previewUrl = `${backendUrl}/api/v1/documents/download/${document.decidingSpecialtyCommissionDocumentName}`;
            window.open(previewUrl, '_blank', 'noopener,noreferrer');
        } else {
            toast('Error', {
                description: 'Cannot preview file: Specialty Commission document name or backend URL is missing.',
            });
        }
    };

    if (isLoading) {
        return <div>Loading document details...</div>;
    }

    if (!document) {
        return <div className="text-red-600">Failed to load document details. Please try again later.</div>;
    }

    return (
        <div className="bg-[#1a0500] min-h-screen text-white p-6">
            <Card className="bg-[#2a1000] border-[#ff6600] border-2 shadow-lg shadow-[#ff6600]/20">
                <CardHeader className="border-b border-[#ff6600]/30 pb-4">
                    <CardTitle className="text-[#ff6600] text-2xl">
                        {document.lawName || "Document Preview"}
                    </CardTitle>
                    {document.createdDate && (
                        <p className="text-[#ff9966]">
                            Created on: {new Date(document.createdDate).toLocaleDateString('en-US', { 
                                year: 'numeric', 
                                month: 'long', 
                                day: 'numeric' 
                            })}
                        </p>
                    )}
                </CardHeader>
                <CardContent className="pt-6">
                    {document.decidingSpecialtyCommissionDocumentName ? (
                        <div className="space-y-6">
                            <div className="bg-[#3a2010] p-4 rounded-lg border border-[#ff6600]/30">
                                <h3 className="text-[#ff9966] text-lg font-medium mb-2">Specialty Commission Document</h3>
                                <p className="text-white/80 mb-4">{document.decidingSpecialtyCommissionDocumentName}</p>
                                <Button
                                    onClick={handleSpecialtyCommissionPreview}
                                    className="bg-[#ff6600] hover:bg-[#ff8533] text-white"
                                >
                                    <Eye className="h-4 w-4 mr-2" />
                                    Preview Document
                                </Button>
                            </div>
                            
                            {document.debateAndApprovalStartDate && (
                                <div className="bg-[#3a2010] p-4 rounded-lg border border-[#ff6600]/30">
                                    <h3 className="text-[#ff9966] text-lg font-medium mb-2">Debate Information</h3>
                                    <p className="text-white/80">
                                        Debate started on: {new Date(document.debateAndApprovalStartDate).toLocaleString()}
                                    </p>
                                </div>
                            )}
                        </div>
                    ) : (
                        <div className="bg-[#3a2010] p-4 rounded-lg border border-[#ff6600]/30 text-center">
                            <p className="text-white/80">No specialty commission document available for preview.</p>
                        </div>
                    )}
                </CardContent>
            </Card>
            
            <div className="mt-6">
                <Table className="bg-[#2a1000] border border-[#ff6600]/30 rounded-lg overflow-hidden">
                    <TableHeader className="bg-[#3a2010]">
                        <TableRow className="border-b border-[#ff6600]/30">
                            <TableHead className="text-[#ff9966]">Document ID</TableHead>
                            <TableHead className="text-[#ff9966]">Document Name</TableHead>
                            <TableHead className="text-[#ff9966]">Created Date</TableHead>
                            <TableHead className="text-[#ff9966] text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        <TableRow className="border-b border-[#ff6600]/20 hover:bg-[#3a2010]/50">
                            <TableCell className="font-medium text-white">{document.id}</TableCell>
                            <TableCell className="text-white">
                                {document.lawName || "Unnamed Document"}
                            </TableCell>
                            <TableCell className="text-white/80">
                                {document.createdDate
                                    ? new Date(document.createdDate).toLocaleString()
                                    : 'N/A'
                                }
                            </TableCell>
                            <TableCell className="text-right">
                                {document.decidingSpecialtyCommissionDocumentName && (
                                    <Button
                                        onClick={handleSpecialtyCommissionPreview}
                                        variant="outline"
                                        className="border-[#ff6600] text-[#ff6600] hover:bg-[#ff6600] hover:text-white"
                                    >
                                        <Eye className="h-4 w-4 mr-1" />
                                        Preview
                                    </Button>
                                )}
                            </TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}

export default NextStageRequestPage;

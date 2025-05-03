import { useEffect, useState } from 'react';
import { Button } from "@/components/ui/button"; // Assuming Button is styled via ShadCN/Tailwind
import axios from 'axios';
import { toast } from "sonner";
import { useParams } from "react-router-dom";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"; // Assuming Table components are styled
import { IDocument } from "@/bemodel/Api"; // Ensure this path is correct
import { Eye } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"; // Assuming Card components are styled

function NextStageRequestPage() {
    const { requestId: documentIdParam } = useParams<{ requestId: string }>();
    const [document, setDocument] = useState<IDocument | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const backendUrl = import.meta.env.VITE_BACKEND_URL;

    useEffect(() => {
        fetchDocument();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [documentIdParam]); // Dependency on the param from URL

    const fetchDocument = () => {
        if (documentIdParam) {
            setIsLoading(true); // Set loading true before fetch

            axios.get(`${backendUrl}/api/v1/documents/${documentIdParam}`, {
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
                        // Consider adding specific styling for error toast if needed
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
        // Simple loading text, inherits page text color
        return <div className="p-6 text-gray-700">Loading document details...</div>;
    }

    if (!document) {
        // Error message styling
        return <div className="p-6 text-red-600">Failed to load document details. Please try again later.</div>;
    }

    // Define dark orange color for accents (Tailwind classes)
    const accentColor = 'orange-700'; // e.g., text-orange-700, border-orange-700, bg-orange-700
    const accentHoverColor = 'orange-800'; // e.g., hover:bg-orange-800
    const accentLightColor = 'orange-600'; // Lighter shade if needed, e.g., text-orange-600

    return (
        // Main container: white background, dark text
        <div className="bg-white min-h-screen text-gray-900 p-6">
            {/* Card: white background, dark orange border/shadow accent */}
            <Card className={`bg-white border-${accentColor} border-2 shadow-lg shadow-${accentColor}/20`}>
                {/* Card Header: light gray bottom border */}
                <CardHeader className="border-b border-gray-200 pb-4">
                    {/* Card Title: dark orange text */}
                    <CardTitle className={`text-${accentColor} text-2xl`}>
                        {document.lawName || "Document Preview"}
                    </CardTitle>
                    {document.createdDate && (
                        // Secondary text: medium gray
                        <p className="text-gray-600 mt-1">
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
                            {/* Inner Box: very light gray background, subtle border */}
                            <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                {/* Inner Heading: dark orange text */}
                                <h3 className={`text-${accentColor} text-lg font-medium mb-2`}>Discussion chamber</h3>
                                {/* Primary text inside box */}
                                <p className="text-gray-800 mb-4">{document.decidingSpecialtyCommissionDocumentName}</p>
                                {/* Primary Button: dark orange background, white text */}
                                <Button
                                    onClick={handleSpecialtyCommissionPreview}
                                    className={`bg-${accentColor} hover:bg-${accentHoverColor} text-white`}
                                >
                                    <Eye className="h-4 w-4 mr-2" />
                                    Preview Document
                                </Button>
                            </div>

                            {document.debateAndApprovalStartDate && (
                                <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                    <h3 className={`text-${accentColor} text-lg font-medium mb-2`}>Debate Information</h3>
                                    <p className="text-gray-700">
                                        Debate started on: {new Date(document.debateAndApprovalStartDate).toLocaleString()}
                                    </p>
                                </div>
                            )}
                        </div>
                    ) : (
                        // "No document" Box: similar styling to other inner boxes
                        <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 text-center">
                            <p className="text-gray-600">No specialty commission document available for preview.</p>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}

export default NextStageRequestPage;

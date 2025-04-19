import React, { useState } from 'react';
import {Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {IRequest} from "@/bemodel/Api.ts";
import {Button} from "@/components/ui/button.tsx";
import DocumentComments from "@/components/comments/DocumentComments";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

interface UserDocumentsTableProps {
    request: IRequest | null;
}

const RequestsDocumentTable: React.FC<UserDocumentsTableProps> = ({request}) => {
    const [selectedDocumentId, setSelectedDocumentId] = useState<number | null>(null);
    const [isCommentsOpen, setIsCommentsOpen] = useState(false);

    const handlePreview = (documentName: string | undefined) => {
        if (documentName) {
            window.open(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/${documentName}`, '_blank');
        }
    };

    const handleViewComments = (documentId: number | undefined) => {
        if (documentId) {
            setSelectedDocumentId(documentId);
            setIsCommentsOpen(true);
        }
    };

    if (!request || !request.documents || request.documents.length === 0) {
        return <p>No documents available for this request.</p>;
    }

    return (
        <>
            <Table>
                <TableCaption>{`Documents of Request: ${request.id}`}</TableCaption>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[100px]">Document ID</TableHead>
                        <TableHead>Stored Document Name</TableHead>
                        <TableHead>Created Date</TableHead>
                        <TableHead>Action</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {request.documents.map((document) => (
                        <TableRow key={document.id}>
                            <TableCell className="font-medium">{document.id}</TableCell>
                            <TableCell>{document.storedDocumentName}</TableCell>
                            <TableCell>{document.createdDate}</TableCell>
                            <TableCell>
                                <div className="flex space-x-2">
                                    <Button
                                        onClick={() => handlePreview(document.storedDocumentName)}
                                    >
                                        Preview
                                    </Button>
                                    <Button
                                        variant="outline"
                                        onClick={() => handleViewComments(document.id)}
                                    >
                                        Comments
                                    </Button>
                                </div>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            <Dialog open={isCommentsOpen} onOpenChange={setIsCommentsOpen}>
                <DialogContent className="max-w-3xl">
                    <DialogHeader>
                        <DialogTitle>Document Comments</DialogTitle>
                    </DialogHeader>
                    {selectedDocumentId && (
                        <DocumentComments documentId={selectedDocumentId} />
                    )}
                </DialogContent>
            </Dialog>
        </>
    );
};

export default RequestsDocumentTable;

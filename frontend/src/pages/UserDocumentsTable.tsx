import React from 'react';
import {Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {IUserDTO} from "@/bemodel/Api.ts";
import {Button} from "@/components/ui/button.tsx";

interface UserDocumentsTableProps {
    user: IUserDTO | null;
}

const UserDocumentsTable: React.FC<UserDocumentsTableProps> = ({user}) => {
    const handlePreview = (documentName: string | undefined) => {
        if (documentName) {
            window.open(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/${documentName}`, '_blank');
        }
    };

    if (!user || !user.documents || user.documents.length === 0) {
        return <p>No documents available for this user.</p>;
    }

    return (
        <Table>
            <TableCaption>{`Documents of User ID: ${user.keycloakId}`}</TableCaption>
            <TableHeader>
                <TableRow>
                    <TableHead className="w-[100px]">Document ID</TableHead>
                    <TableHead>Stored Document Name</TableHead>
                    <TableHead>Created Date</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Action</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {user.documents.map((document) => (
                    <TableRow key={document.id}>
                        <TableCell className="font-medium">{document.id}</TableCell>
                        <TableCell>{document.storedDocumentName}</TableCell>
                        <TableCell>{document.createdDate}</TableCell>
                        <TableCell>{document.status}</TableCell>
                        <TableCell>
                            <Button
                                onClick={() => handlePreview(document.storedDocumentName)}
                            >
                                Preview
                            </Button>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};

export default UserDocumentsTable;

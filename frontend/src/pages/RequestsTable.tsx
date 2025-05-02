import {useEffect, useState} from 'react';
import axios from 'axios';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.tsx";
import {Button} from "@/components/ui/button.tsx";
import {useNavigate} from "react-router-dom";
import {useAppSelector} from "@/hooks.ts";
import {IDocument} from "@/bemodel/Api.ts";

function RequestsTable() {
    const navigate = useNavigate();
    const [reviewRequests, setReviewRequests] = useState<IDocument[]>([]);
    const userId = useAppSelector((state) => state.core.authenticatedUserId);

    useEffect(() => {
        if (userId) {
            fetchDocumentsThatNeedReview();
        }
    }, [userId]);

    const fetchDocumentsThatNeedReview = () => {
        axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/need-review`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setReviewRequests(response.data);
            })
            .catch((error) => {
                console.error('Error fetching review requests:', error);
            });
    }

    const openRequestPage = (request: IDocument) => {
        navigate(`/request/${request.id}`);
    }

    return (
        <>
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[100px]">Document ID</TableHead>
                        <TableHead>Created Date</TableHead>
                        <TableHead className="text-center">Preview</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {reviewRequests.map((document) => (
                        // The map function returns TableRow elements directly
                        <TableRow key={document.id}>
                            <TableCell className="font-medium">{document.id}</TableCell>
                            <TableCell>
                                {document.createdDate
                                    ? new Date(document.createdDate).toLocaleString()
                                    : 'N/A'
                                }
                            </TableCell>
                            <TableCell className="text-center">
                                <Button
                                    onClick={() => openRequestPage(document)}
                                >
                                    Open
                                </Button>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </>
    );
}

export default RequestsTable;

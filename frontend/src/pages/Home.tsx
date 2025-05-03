import RequestsTable from "@/pages/RequestsTable.tsx";
import {Button} from "@/components/ui/button.tsx";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";

function Home() {
    const navigate = useNavigate()
    // const isJournalist = useUserHasRole('JOURNALIST');
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [documentName, setDocumentName] = useState("");

    const openDialog = () => {
        setIsDialogOpen(true);
    };

    const createNewRequest = () => {
        if (!documentName.trim()) {
            alert("Please enter a document name");
            return;
        }

        axios.post(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/create-document?documentName=${encodeURIComponent(documentName)}`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
                setIsDialogOpen(false);
                setDocumentName("");
                navigate(`/request/${response.data.id}`);
            })
            .catch((error) => {
                console.error('Error:', error);
            });
    }

    // if the user is a journalist, show a button to start a new request and the list of previous requests
    // if the user is juridic show a list of requests
    // if the user is director show a list of requests
    return (
        <>
            <RequestsTable/>
            {/*{isJournalist && <Button onClick={openDialog}>New request</Button>}*/}
            <Button onClick={openDialog}>New request</Button>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Create New Document</DialogTitle>
                    </DialogHeader>
                    <div className="py-4">
                        <label htmlFor="documentName" className="block text-sm font-medium mb-2">
                            Document Name
                        </label>
                        <Input
                            id="documentName"
                            value={documentName}
                            onChange={(e) => setDocumentName(e.target.value)}
                            placeholder="Enter document name"
                            className="w-full"
                        />
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={createNewRequest}>
                            Create
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </>
    );
}

export default Home;

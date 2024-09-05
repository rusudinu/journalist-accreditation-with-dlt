import RequestsTable from "@/pages/RequestsTable.tsx";
import {useUserHasRole} from "@/common/auth/UserUtils.ts";
import {Button} from "@/components/ui/button.tsx";
import axios from "axios";
import {useNavigate} from "react-router-dom";

function Home() {
    const navigate = useNavigate()
    const isJournalist = useUserHasRole('JOURNALIST');
    
    const createNewRequest = () => {
        axios.post(`${import.meta.env.VITE_BACKEND_URL}/api/v1/requests`, {
            headers: {
                'Content-Type': 'application/json',
            },
        })
            .then((response) => {
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
            {isJournalist && <Button onClick={createNewRequest}>New request</Button>}
        </>
    );
}

export default Home;

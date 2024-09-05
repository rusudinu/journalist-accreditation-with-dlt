import {useEffect} from 'react';
import axios from 'axios';
import {useUserHasRole} from "@/common/auth/UserUtils.ts";

function Home() {
    const isJournalist = useUserHasRole('JOURNALIST');
    const isJuridic = useUserHasRole('JURIDIC');
    const isDirector = useUserHasRole('DIRECTOR');


    useEffect(() => {

    }, []);

    // if the user is a journalist, show a button to start a new request and the list of previous requests
    // if the user is juridic show a list of requests
    // if the user is director show a list of requests
    return (
        <>

        </>
    );
}

export default Home;

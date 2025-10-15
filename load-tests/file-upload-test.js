import http from 'k6/http';
import {check} from 'k6';

export const options = {
    vus: 20,
    iterations: 1000,
};

const document = open('./test-document.pdf', 'b');

export default function () {
    // define URL and request body
    const url = 'http://localhost:8080/api/v1/documents';

    const data = {
        file: http.file(document, 'test-document.pdf'),
        documentId: '11',
    }

    const params = {
        headers: {
            'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0TnhCS1VRZjI3MzFQblhfM3JJZ045NkdKX2hqQ1pJdmVJYjBjSERfZU5RIn0.eyJleHAiOjE3MjYwMzU3NDIsImlhdCI6MTcyNjAzNTQ0MiwiYXV0aF90aW1lIjoxNzI2MDM0MjI3LCJqdGkiOiIxY2ZhMTIyMy1iNzdjLTRlYzktODkyNy0yYTgwYjc1NGY1ZGUiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEvcmVhbG1zL2pvdXJuYWxpc3QtYWNjcmVkaXRhdGlvbiIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI3Y2Q0NGNjOC04N2FhLTRjMDgtODFiMC1kYWE2MGM4YzQ3NGMiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJqb3VybmFsaXN0LWFjY3JlZGl0YXRpb24iLCJub25jZSI6IjBkZTZlODA1LTJjODItNGVmMS04ODM2LTNjYmEwNTA2YWQwMSIsInNlc3Npb25fc3RhdGUiOiIxMDU3ZDcyZC03NGVmLTQxZWMtYjAzYy0xZmFkYTRmNjEwMzAiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWpvdXJuYWxpc3QtYWNjcmVkaXRhdGlvbiIsIkpPVVJOQUxJU1QiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwic2lkIjoiMTA1N2Q3MmQtNzRlZi00MWVjLWIwM2MtMWZhZGE0ZjYxMDMwIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJNciBKb3VybmFsaXN0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoiam91cm5hbGlzdCIsImdpdmVuX25hbWUiOiJNciIsImZhbWlseV9uYW1lIjoiSm91cm5hbGlzdCIsImVtYWlsIjoiam91cm5hbGlzdEBqb3VybmFsaXN0LmNvbSJ9.06gxYRFs7X2GJ1UgVjZumMVwfQJCKdt7PsGipjAIFwavibwJRc6WHVGQ_w9H639SkSxQp4QBqVH_c3vWIzfRt3KD0h_mbMsc-LvUeENltXBq-NqfRNxCZFHtoBC88MqBgbs1lCfMCufbD6t_iRbEk9uW-i18Jo6gOn50NtO7TTB1h5fVdkszptAAKos1E2FX44y9EHMcyfiytGdKYNQiMVD6tH3e_efuZFj09yRYiaGh7vRTSkICToFliIowSaoQ-LoLEIbIQclOyWoQovPonjk-eHVFDo6_yhw6KHITSO6ZhEzhRmx-8yWsSev9-1S-JpmLzoeLWY-T_DcsHGDtkQ'
        },
    };

    // send a post request and save response as a variable
    const res = http.post(url, data, params);

    // check that response is 200
    check(res, {
        'response code was 200': (res) => res.status == 200,
    });
}

/*
import http from 'k6/http';
import {check} from 'k6';

// do 100 iterations of the test
export const options = {
    vus: 1000,
    duration: '1m',
};

export default function () {
    // define URL and request body
    const url = 'http://localhost:8080/quick/demo';

    // send a post request and save response as a variable
    const res = http.get(url);

    // check that response is 200
    check(res, {
        'response code was 200': (res) => res.status == 200,
    });
}
*/

import http from 'k6/http';
import {check} from 'k6';
import {FormData} from 'https://jslib.k6.io/formdata/0.0.2/index.js';
// open the file in binary mode
const document = open('./test-document.pdf', 'b');

export default function () {
    // define URL and request body
    const url = 'http://localhost:8080/api/v1/documents?status=CREATED&requestId=11';

    const data = {
        file: http.file(document, 'test-document.pdf'),
    }

    const params = {
        headers: {
            'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0TnhCS1VRZjI3MzFQblhfM3JJZ045NkdKX2hqQ1pJdmVJYjBjSERfZU5RIn0.eyJleHAiOjE3MjYwMzQ4OTUsImlhdCI6MTcyNjAzNDU5NSwiYXV0aF90aW1lIjoxNzI2MDM0MjI3LCJqdGkiOiIxYTU4OWU1NS0zNGU3LTRiNTgtOGFjNi0xOTJlZDJmNTZkOGIiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEvcmVhbG1zL2pvdXJuYWxpc3QtYWNjcmVkaXRhdGlvbiIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI3Y2Q0NGNjOC04N2FhLTRjMDgtODFiMC1kYWE2MGM4YzQ3NGMiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJqb3VybmFsaXN0LWFjY3JlZGl0YXRpb24iLCJub25jZSI6IjIxNDkxYzkwLWQ4ODQtNGUwYi04ZjVkLWI0OTRkOTRhNDYyZSIsInNlc3Npb25fc3RhdGUiOiIxMDU3ZDcyZC03NGVmLTQxZWMtYjAzYy0xZmFkYTRmNjEwMzAiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWpvdXJuYWxpc3QtYWNjcmVkaXRhdGlvbiIsIkpPVVJOQUxJU1QiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwic2lkIjoiMTA1N2Q3MmQtNzRlZi00MWVjLWIwM2MtMWZhZGE0ZjYxMDMwIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJNciBKb3VybmFsaXN0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoiam91cm5hbGlzdCIsImdpdmVuX25hbWUiOiJNciIsImZhbWlseV9uYW1lIjoiSm91cm5hbGlzdCIsImVtYWlsIjoiam91cm5hbGlzdEBqb3VybmFsaXN0LmNvbSJ9.rO5J98M_gTvwbSk3dDUoAP7l-6V8S1e8O36UBX1D7qlHvkwzK2V0o7L9SeOqeD2Wexa9FDgDXO19IHjU8Dkwf_KS9ObBrcoDtwiSVAPS8CAi-S9_6fKSEbzCZtLUxj5lW9xoaRygRt_VuI2yI10Z2_Pscp5qIrl54NGMx5RSgq8ng81RiQHx64JqPDJTriTrbWBBeEfsCKNKmztpuEEoLzg1U6wQahZxX2scSlOLN0_JAHzz12Ah-6tvZ097ljW5MwXTlq3TIb2aZbPJDvYK3L7Io95AiQmKJriXJhXBk47S9F-Pf7GBiG4lMIM35y_Ez_AX_MKCKWGb07dYXVHv7g'
        },
    };

    // send a post request and save response as a variable
    const res = http.post(url, data, params);

    // check that response is 200
    check(res, {
        'response code was 200': (res) => res.status == 200,
    });
}

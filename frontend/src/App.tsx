import {useContext, useState} from 'react';
import './App.css';
import {
    FileUploader,
    FileUploaderContent,
    FileUploaderItem,
    FileInput,
} from "@/components/extension/file-uploader";
import {DropzoneOptions} from "react-dropzone";
import {Button} from "@/components/ui/button.tsx";
import {AuthContext} from "@/hoc/AuthWrapper.tsx";
import axios from 'axios';

const FileSvgDraw = () => {
    return (
        <>
            <svg
                className="w-8 h-8 mb-3 text-gray-500 dark:text-gray-400"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 20 16"
            >
                <path
                    stroke="currentColor"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M13 13h3a3 3 0 0 0 0-6h-.025A5.56 5.56 0 0 0 16 6.5 5.5 5.5 0 0 0 5.207 5.021C5.137 5.017 5.071 5 5 5a4 4 0 0 0 0 8h2.167M10 15V6m0 0L8 8m2-2 2 2"
                />
            </svg>
            <p className="mb-1 text-sm text-gray-500 dark:text-gray-400">
                <span className="font-semibold">Click to upload</span>
                &nbsp; or drag and drop
            </p>
            <p className="text-xs text-gray-500 dark:text-gray-400">
                A document in PDF, DOC, DOCX, JPG, JPEG, PNG format
            </p>
        </>
    );
};

function App() {
    const keycloak = useContext(AuthContext);
    const [files, setFiles] = useState<File[] | null>([]);

    const dropzone = {
        accept: {
            "image/*": [".jpg", ".jpeg", ".png"],
            "application/pdf": [".pdf"],
            "application/msword": [".doc"],
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document": [".docx"],
        },
        multiple: false,
        maxSize: 1024 * 1024,
    } satisfies DropzoneOptions;

    const testJournalist = async () => {
        try {
            const response = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/only-journalist`, {});
        } catch (error) {
            console.error("Error fetching journalist documents:", error);
        }
    }

    const testMinistry = async () => {
        try {
            const response = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/only-ministry`, {});
        } catch (error) {
            console.error("Error fetching ministry documents:", error);
        }
    }

    const testBoth = async () => {
        try {
            const response = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents/journalist-and-ministry`, {});
        } catch (error) {
            console.error("Error fetching journalist and ministry documents:", error);
        }
    }

    const handleUpload = async () => {
        if (!files || files.length === 0) {
            alert("Please select files to upload.");
            return;
        }

        const formData = new FormData();
        files.forEach(file => {
            formData.append("file", file);
        });

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/api/v1/documents`, {
                method: "POST",
                body: formData,
            });

            if (response.ok) {
                alert("Files uploaded successfully!");
                setFiles([]); // Clear files after successful upload
            } else {
                alert("Failed to upload files.");
            }
        } catch (error) {
            console.error("Error uploading files:", error);
            alert("An error occurred while uploading the files.");
        }
    };

    return (
        <>
            <FileUploader
                value={files}
                onValueChange={setFiles}
                dropzoneOptions={dropzone}
            >
                <FileInput className="border bg-background rounded-md">
                    <div className="flex items-center justify-center flex-col pt-3 pb-4 w-full">
                        <FileSvgDraw/>
                    </div>
                </FileInput>
                <FileUploaderContent className="flex items-center flex-row gap-2">
                    {files?.map((file, i) => {
                        const fileType = file.type;
                        const isImage = fileType.startsWith("image/");
                        const isPDF = fileType === "application/pdf";

                        return (
                            <FileUploaderItem
                                key={i}
                                index={i}
                                className="size-20 p-0 rounded-md overflow-hidden"
                                aria-roledescription={`file ${i + 1} containing ${file.name}`}
                            >
                                {isImage ? (
                                    <img
                                        src={URL.createObjectURL(file)}
                                        alt={file.name}
                                        height={80}
                                        className="size-20 p-0"
                                    />
                                ) : isPDF ? (
                                    <div className="flex items-center justify-center h-20 w-20 bg-gray-200 text-gray-700">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="size-6">
                                            <path strokeLinecap="round" strokeLinejoin="round"
                                                  d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z"/>
                                        </svg>

                                    </div>
                                ) : (
                                    <div className="flex items-center justify-center h-20 w-20 bg-gray-200 text-gray-700">
                                        <span>File</span>
                                    </div>
                                )}
                            </FileUploaderItem>
                        );
                    })}
                </FileUploaderContent>

            </FileUploader>
            <Button
                onClick={handleUpload}
                className="mt-4"
            >
                Upload File
            </Button>
            <Button
                onClick={testJournalist}
                className="mt-4"
            >T JOURNAL</Button>
            <Button
                onClick={testMinistry}
                className="mt-4"
            >
                T MIN
            </Button>
            <Button
                onClick={testBoth}
                className="mt-4"
            >
                T BOTH
            </Button>
        </>
    );
}

export default App;

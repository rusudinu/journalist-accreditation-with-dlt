import {useState} from 'react';
import './App.css';
import {
    FileUploader,
    FileUploaderContent,
    FileUploaderItem,
    FileInput,
} from "@/components/extension/file-uploader";
import {DropzoneOptions} from "react-dropzone";

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
    const [files, setFiles] = useState<File[] | null>([]);

    const dropzone = {
        accept: {
            "image/*": [".jpg", ".jpeg", ".png"],
            "application/pdf": [".pdf"],
            "application/msword": [".doc"],
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document": [".docx"],
        },
        multiple: true,
        maxFiles: 4,
        maxSize: 1 * 1024 * 1024,
    } satisfies DropzoneOptions;

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
                    {files?.map((file, i) => (
                        <FileUploaderItem
                            key={i}
                            index={i}
                            className="size-20 p-0 rounded-md overflow-hidden"
                            aria-roledescription={`file ${i + 1} containing ${file.name}`}
                        >
                            <img
                                src={URL.createObjectURL(file)}
                                alt={file.name}
                                height={80}
                                className="size-20 p-0"
                            />
                        </FileUploaderItem>
                    ))}
                </FileUploaderContent>
            </FileUploader>
            <button
                onClick={handleUpload}
                className="mt-4 p-2 bg-blue-500 text-white rounded"
            >
                Upload Files
            </button>
        </>
    );
}

export default App;

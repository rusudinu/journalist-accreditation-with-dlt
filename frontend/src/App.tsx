import {useState} from 'react'
import './App.css'
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
            "document/*": [".pdf", ".doc", ".docx"],
        },
        multiple: true,
        maxFiles: 4,
        maxSize: 1 * 1024 * 1024,
    } satisfies DropzoneOptions;

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
        </>
    )
}

export default App

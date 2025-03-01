import React, { useEffect, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { verifiableCredentialService, VerifiableCredential } from '../../services/VerifiableCredentialService';
import { Button } from '../ui/button';
import { Alert, AlertDescription, AlertTitle } from '../ui/alert';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Loader2 } from 'lucide-react';

interface CredentialQRCodeProps {
    requestId: string;
}

/**
 * Component to display a verifiable credential as a QR code
 */
export function CredentialQRCode({ requestId }: CredentialQRCodeProps) {
    const [loading, setLoading] = useState<boolean>(true);
    const [credential, setCredential] = useState<VerifiableCredential | null>(null);
    const [error, setError] = useState<string | null>(null);

    const fetchCredential = async () => {
        setLoading(true);
        setError(null);
        try {
            const result = await verifiableCredentialService.getLatestCredential(requestId);
            setCredential(result);
        } catch (err: unknown) {
            setError('Could not load the credential');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCredential();
    }, [requestId]);

    if (loading) {
        return (
            <Card className="w-full max-w-md mx-auto">
                <CardHeader>
                    <CardTitle>Loading Credential</CardTitle>
                    <CardDescription>Please wait while we fetch the verifiable credential</CardDescription>
                </CardHeader>
                <CardContent className="flex justify-center items-center h-48">
                    <Loader2 className="h-8 w-8 animate-spin" />
                </CardContent>
            </Card>
        );
    }

    if (error || !credential) {
        return (
            <Card className="w-full max-w-md mx-auto">
                <CardHeader>
                    <CardTitle>No Credential Available</CardTitle>
                    <CardDescription>There is no verifiable credential for this request yet</CardDescription>
                </CardHeader>
                <CardContent>
                    <Alert variant="destructive">
                        <AlertTitle>Credential Not Found</AlertTitle>
                        <AlertDescription>
                            {error || 'This request does not have a verifiable credential associated with it yet.'}
                        </AlertDescription>
                    </Alert>
                </CardContent>
                <CardFooter>
                    <Button onClick={fetchCredential} variant="outline" className="w-full">Retry</Button>
                </CardFooter>
            </Card>
        );
    }

    // Prepare the credential data for QR code
    const qrData = JSON.stringify({
        id: credential.id,
        issuer: credential.issuer,
        subject: {
            id: credential.credentialSubject.id,
            hash: credential.credentialSubject.fileHash,
            status: credential.credentialSubject.status
        },
        verificationUrl: `${window.location.origin}/verify/${credential.id}`
    });

    return (
        <Card className="w-full max-w-md mx-auto">
            <CardHeader>
                <CardTitle>Verifiable Credential</CardTitle>
                <CardDescription>Scan this QR code to verify the credential</CardDescription>
            </CardHeader>
            <CardContent className="flex justify-center">
                <div className="p-4 bg-white rounded-md">
                    <QRCodeSVG
                        value={qrData}
                        size={200}
                        level="H"
                        includeMargin={true}
                    />
                </div>
            </CardContent>
            <CardFooter className="flex justify-between">
                <Button onClick={fetchCredential} variant="outline">Refresh</Button>
                <Button
                    onClick={() => {
                        // Copy credential ID to clipboard
                        navigator.clipboard.writeText(credential.id);
                        alert('Credential ID copied to clipboard!');
                    }}
                >
                    Copy ID
                </Button>
            </CardFooter>
        </Card>
    );
} 

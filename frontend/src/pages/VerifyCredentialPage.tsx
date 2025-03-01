import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { verifiableCredentialService, VerifiableCredential, VerificationResponse } from '../services/VerifiableCredentialService';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Loader2, CheckCircle, XCircle } from 'lucide-react';

function VerifyCredentialPage() {
    const { credentialId } = useParams<{ credentialId: string }>();
    const [loading, setLoading] = useState<boolean>(true);
    const [credential, setCredential] = useState<VerifiableCredential | null>(null);
    const [verification, setVerification] = useState<VerificationResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    const fetchAndVerifyCredential = async () => {
        setLoading(true);
        setError(null);
        
        try {
            if (!credentialId) {
                setError('No credential ID provided');
                setLoading(false);
                return;
            }

            // Fetch the credential by ID
            const fetchedCredential = await verifiableCredentialService.findByCredentialId(credentialId);
            
            if (!fetchedCredential) {
                setError('Credential not found');
                setLoading(false);
                return;
            }
            
            setCredential(fetchedCredential);
            
            // Verify the credential
            const verificationResult = await verifiableCredentialService.verifyCredential(fetchedCredential);
            setVerification(verificationResult);
        } catch (err: unknown) {
            setError('Failed to verify credential');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAndVerifyCredential();
    }, [credentialId]);

    if (loading) {
        return (
            <div className="container mx-auto py-8">
                <Card className="max-w-2xl mx-auto">
                    <CardHeader>
                        <CardTitle>Verifying Credential</CardTitle>
                        <CardDescription>Please wait while we verify the credential</CardDescription>
                    </CardHeader>
                    <CardContent className="flex justify-center items-center h-48">
                        <Loader2 className="h-8 w-8 animate-spin" />
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (error || !credential || !verification) {
        return (
            <div className="container mx-auto py-8">
                <Card className="max-w-2xl mx-auto">
                    <CardHeader>
                        <CardTitle>Verification Failed</CardTitle>
                        <CardDescription>We couldn't verify this credential</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <Alert variant="destructive">
                            <XCircle className="h-4 w-4" />
                            <AlertTitle>Error</AlertTitle>
                            <AlertDescription>
                                {error || 'An unknown error occurred during verification.'}
                            </AlertDescription>
                        </Alert>
                    </CardContent>
                    <CardFooter>
                        <Button onClick={fetchAndVerifyCredential} variant="outline">Retry</Button>
                    </CardFooter>
                </Card>
            </div>
        );
    }

    return (
        <div className="container mx-auto py-8">
            <Card className="max-w-2xl mx-auto">
                <CardHeader>
                    <div className="flex justify-between items-center">
                        <div>
                            <CardTitle>Credential Verification</CardTitle>
                            <CardDescription>Results of credential verification</CardDescription>
                        </div>
                        <Badge variant={verification.valid ? 'default' : 'destructive'}>
                            {verification.valid ? 'Valid' : 'Invalid'}
                        </Badge>
                    </div>
                </CardHeader>
                <CardContent>
                    {verification.valid ? (
                        <Alert>
                            <CheckCircle className="h-4 w-4" />
                            <AlertTitle>Valid Credential</AlertTitle>
                            <AlertDescription>{verification.message}</AlertDescription>
                        </Alert>
                    ) : (
                        <Alert variant="destructive">
                            <XCircle className="h-4 w-4" />
                            <AlertTitle>Invalid Credential</AlertTitle>
                            <AlertDescription>{verification.message}</AlertDescription>
                        </Alert>
                    )}

                    <div className="mt-6">
                        <h3 className="text-lg font-semibold mb-2">Credential Details</h3>
                        <div className="space-y-4">
                            <div>
                                <p className="text-sm text-muted-foreground">ID</p>
                                <p>{credential.id}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Issuer</p>
                                <p>{credential.issuer}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Issuance Date</p>
                                <p>{new Date(credential.issuanceDate).toLocaleString()}</p>
                            </div>
                            <Separator />
                            <div>
                                <p className="text-sm text-muted-foreground">Subject ID</p>
                                <p>{credential.credentialSubject.id}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">File Hash</p>
                                <p className="break-all">{credential.credentialSubject.fileHash}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Status</p>
                                <p>{credential.credentialSubject.status}</p>
                            </div>
                            <Separator />
                            <div>
                                <p className="text-sm text-muted-foreground">Verification Timestamp</p>
                                <p>{new Date(verification.verifiedAt).toLocaleString()}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Verified By</p>
                                <p>{verification.verifiedBy}</p>
                            </div>
                        </div>
                    </div>
                </CardContent>
                <CardFooter>
                    <Button onClick={fetchAndVerifyCredential} variant="outline">Verify Again</Button>
                </CardFooter>
            </Card>
        </div>
    );
}

export default VerifyCredentialPage; 
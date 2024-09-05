type RSAKeyPair = {
    publicKey: CryptoKey;
    privateKey: CryptoKey;
}

/*
* This function generates a new RSA key pair if one does not exist in local storage,
* otherwise it retrieves the existing key pair from local storage.
* It returns the public and private keys as a CryptoKey object.
 */
export const cryptoGetRSAKeyPair = async (): Promise<RSAKeyPair> => {
    let keyPair;
    let localStorageKeyPair = window.localStorage.getItem('keyPair');
    if (!localStorageKeyPair) {
        let generatedKeyPair = await window.crypto.subtle.generateKey(
            {
                name: 'RSASSA-PKCS1-v1_5',
                modulusLength: 2048,
                publicExponent: new Uint8Array([0x01, 0x00, 0x01]),
                hash: 'SHA-256',
            },
            true,
            ['sign', 'verify']
        );

        const exportedPublicKey = await exportCryptoKey(generatedKeyPair.publicKey);
        const exportedPrivateKey = await exportCryptoKey(generatedKeyPair.privateKey);

        window.localStorage.setItem('keyPair', JSON.stringify({
            publicKey: exportedPublicKey,
            privateKey: exportedPrivateKey
        }));

        keyPair = generatedKeyPair;
    } else {
        const parsedKeyPair = JSON.parse(localStorageKeyPair);
        const publicKey = await importCryptoKey(parsedKeyPair.publicKey, false);
        const privateKey = await importCryptoKey(parsedKeyPair.privateKey, true);

        keyPair = {publicKey, privateKey};
    }
    return keyPair;
}

/*
* This function signs a diploma object using an RSA private key.
* It can be called with a keyPair argument to use a specific key pair,
* otherwise it will use the getRSAKeyPair function to retrieve the key pair from local storage or
* generate a new one if it does not exist.
 */
export const cryptoSignDocument = async (documentBuffer: ArrayBuffer, keyPair: RSAKeyPair | null = null): Promise<string> => {
    if (!keyPair) {
        keyPair = await cryptoGetRSAKeyPair();
    }
    let hash = await window.crypto.subtle.digest('SHA-256', documentBuffer);
    let digitalSignature = await window.crypto.subtle.sign(
        {
            name: 'RSASSA-PKCS1-v1_5',
        },
        keyPair.privateKey,
        hash
    );
    return btoa(String.fromCharCode(...new Uint8Array(digitalSignature)));
}

/*
* This function verifies the signature of a diploma object using an RSA public key.
* It returns a boolean value indicating whether the signature is valid.
 */
export const cryptoVerifyDocument = async (documentBuffer: ArrayBuffer, signature: string, publicKey: CryptoKey): Promise<boolean> => {
    let hash = await window.crypto.subtle.digest('SHA-256', documentBuffer);
    let digitalSignature = new Uint8Array(atob(signature).split('').map(c => c.charCodeAt(0)));
    return window.crypto.subtle.verify(
        {
            name: 'RSASSA-PKCS1-v1_5',
        },
        publicKey,
        digitalSignature,
        hash
    );
}

const exportCryptoKey = async (key: CryptoKey): Promise<JsonWebKey> => {
    return await window.crypto.subtle.exportKey('jwk', key);
}

const importCryptoKey = async (jwk: JsonWebKey, isPrivate: boolean): Promise<CryptoKey> => {
    return await window.crypto.subtle.importKey(
        'jwk',
        jwk,
        {
            name: 'RSASSA-PKCS1-v1_5',
            hash: 'SHA-256',
        },
        true,
        isPrivate ? ['sign'] : ['verify']
    );
}

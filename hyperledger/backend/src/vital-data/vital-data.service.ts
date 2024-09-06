import * as grpc from '@grpc/grpc-js';
import { connect, Contract, Identity, Signer, signers } from '@hyperledger/fabric-gateway';
import { Injectable } from '@nestjs/common';
import * as crypto from 'crypto';
import { promises as fs } from 'fs';
import * as path from 'path'; // otherwise ERROR [ExceptionHandler] Cannot read properties of undefined (reading 'resolve')
import { TextDecoder } from 'util';
import IChainVitalData from './chain-vital-data.interface';
import { CreateVitalDataInput } from './create-vital-data.input';
import { VitalDataModel } from './vital-data.model';

@Injectable()
export class VitalDataService {
    constructor() {}

    channelName = this.envOrDefault('CHANNEL_NAME', 'mychannel');
    chaincodeName = this.envOrDefault('CHAINCODE_NAME', 'vitaldata');
    mspId = this.envOrDefault('MSP_ID', 'Org1MSP');

    // Path to crypto materials.
    cryptoPath = this.envOrDefault(
        'CRYPTO_PATH',
        path.resolve(__dirname, '..', '..', '..', 'network', 'organizations', 'peerOrganizations', 'org1.example.com'),
    );

    // Path to user private key directory.
    keyDirectoryPath = this.envOrDefault(
        'KEY_DIRECTORY_PATH',
        path.resolve(this.cryptoPath, 'users', 'User1@org1.example.com', 'msp', 'keystore'),
    );

    // Path to user certificate.
    certPath = this.envOrDefault(
        'CERT_PATH',
        path.resolve(this.cryptoPath, 'users', 'User1@org1.example.com', 'msp', 'signcerts', 'User1@org1.example.com-cert.pem'),
    );

    /*
    if running WITHOUT -ca flag when starting the network, then
    certPath = this.envOrDefault('CERT_PATH', path.resolve(this.cryptoPath, 'users', 'User1@org1.example.com', 'msp', 'signcerts', 'User1@org1.example.com-cert.pem'));
     */
    /*
    if running with -ca flag when starting the network, then
    certPath = this.envOrDefault('CERT_PATH', path.resolve(this.cryptoPath, 'users', 'User1@org1.example.com', 'msp', 'signcerts', 'cert.pem'));
     */

    // Path to peer tls certificate.
    tlsCertPath = this.envOrDefault('TLS_CERT_PATH', path.resolve(this.cryptoPath, 'peers', 'peer0.org1.example.com', 'tls', 'ca.crt'));

    // Gateway peer endpoint.
    peerEndpoint = this.envOrDefault('PEER_ENDPOINT', 'localhost:7051');

    // Gateway peer SSL host name override.
    peerHostAlias = this.envOrDefault('PEER_HOST_ALIAS', 'peer0.org1.example.com');

    utf8Decoder = new TextDecoder();
    assetId = `asset${Date.now()}`;

    envOrDefault(key: string, defaultValue: string): string {
        return process.env[key] ?? defaultValue;
    }

    /**
     * displayInputParameters() will print the global scope parameters used by the main driver routine.
     */
    displayInputParameters(): void {
        console.log(`channelName:       ${this.channelName}`);
        console.log(`chaincodeName:     ${this.chaincodeName}`);
        console.log(`mspId:             ${this.mspId}`);
        console.log(`cryptoPath:        ${this.cryptoPath}`);
        console.log(`keyDirectoryPath:  ${this.keyDirectoryPath}`);
        console.log(`certPath:          ${this.certPath}`);
        console.log(`tlsCertPath:       ${this.tlsCertPath}`);
        console.log(`peerEndpoint:      ${this.peerEndpoint}`);
        console.log(`peerHostAlias:     ${this.peerHostAlias}`);
    }

    async newGrpcConnection(): Promise<grpc.Client> {
        const tlsRootCert = await fs.readFile(this.tlsCertPath);
        const tlsCredentials = grpc.credentials.createSsl(tlsRootCert);
        return new grpc.Client(this.peerEndpoint, tlsCredentials, {
            'grpc.ssl_target_name_override': this.peerHostAlias,
        });
    }

    async newIdentity(): Promise<Identity> {
        const credentials = await fs.readFile(this.certPath);
        return { mspId: this.mspId, credentials: credentials };
    }

    async newSigner(): Promise<Signer> {
        const files = await fs.readdir(this.keyDirectoryPath);
        const keyPath = path.resolve(this.keyDirectoryPath, files[0]);
        const privateKeyPem = await fs.readFile(keyPath);
        const privateKey = crypto.createPrivateKey(privateKeyPem);
        return signers.newPrivateKeySigner(privateKey);
    }

    async initLedger(contract: Contract): Promise<void> {
        console.log('\n--> Submit Transaction: InitLedger, function creates the initial set of assets on the ledger');

        await contract.submitTransaction('InitLedger');

        console.log('*** Transaction committed successfully');
    }

    /**
     * Evaluate a transaction to query ledger state.
     */
    async getAllAssets(contract: Contract): Promise<string[]> {
        console.log('\n--> Evaluate Transaction: GetAllAssets, function returns all the current assets on the ledger');
        const resultBytes = await contract.evaluateTransaction('GetAllAssets');
        const resultJson = this.utf8Decoder.decode(resultBytes);
        return JSON.parse(resultJson);
    }

    async getAssetById(contract: Contract, assetID: string): Promise<string> {
        console.log('\n--> Evaluate Transaction: ReadAsset, function returns the asset with the provided ID on the ledger');
        const resultBytes = await contract.evaluateTransaction('ReadStringAsset', assetID);
        const resultJson = this.utf8Decoder.decode(resultBytes);
        return JSON.parse(resultJson);
    }

    async updateVitalData(vitalData: CreateVitalDataInput): Promise<VitalDataModel> {
        console.log('\n--> Submit Transaction: Update Vital Data');
        const client = await this.newGrpcConnection();

        const gateway = connect({
            client,
            identity: await this.newIdentity(),
            signer: await this.newSigner(),
            // Default timeouts for different gRPC calls
            evaluateOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            endorseOptions: () => {
                return { deadline: Date.now() + 15000 }; // 15 seconds
            },
            submitOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            commitStatusOptions: () => {
                return { deadline: Date.now() + 60000 }; // 1 minute
            },
        });

        const network = gateway.getNetwork(this.channelName);

        const contract = network.getContract(this.chaincodeName);

        await contract.submitTransaction(
            'UpdateAsset',
            vitalData.patient,
            vitalData.name,
            vitalData.dateOfBirth,
            vitalData.bloodType,
            vitalData.nameOfGPSurgery,
            vitalData.badReactionsToDrugs,
            vitalData.currentMedications,
            vitalData.currentIllnesses,
        );

        console.log('*** Transaction committed successfully');
        return vitalData;
    }

    async createVitalData(vitalData: CreateVitalDataInput): Promise<VitalDataModel> {
        console.log('\n--> Submit Transaction: Create Vital Data');
        const client = await this.newGrpcConnection();

        const gateway = connect({
            client,
            identity: await this.newIdentity(),
            signer: await this.newSigner(),
            // Default timeouts for different gRPC calls
            evaluateOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            endorseOptions: () => {
                return { deadline: Date.now() + 15000 }; // 15 seconds
            },
            submitOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            commitStatusOptions: () => {
                return { deadline: Date.now() + 60000 }; // 1 minute
            },
        });

        const network = gateway.getNetwork(this.channelName);

        const contract = network.getContract(this.chaincodeName);

        await contract.submitTransaction(
            'CreateAsset',
            vitalData.patient,
            vitalData.name,
            vitalData.dateOfBirth,
            vitalData.bloodType,
            vitalData.nameOfGPSurgery,
            vitalData.badReactionsToDrugs,
            vitalData.currentMedications,
            vitalData.currentIllnesses,
        );

        console.log('*** Transaction committed successfully');
        return vitalData;
    }

    async initChain(): Promise<VitalDataModel[]> {
        console.log(this.cryptoPath);
        // The gRPC client connection should be shared by all Gateway connections to this endpoint.
        const client = await this.newGrpcConnection();

        const gateway = connect({
            client,
            identity: await this.newIdentity(),
            signer: await this.newSigner(),
            // Default timeouts for different gRPC calls
            evaluateOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            endorseOptions: () => {
                return { deadline: Date.now() + 15000 }; // 15 seconds
            },
            submitOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            commitStatusOptions: () => {
                return { deadline: Date.now() + 60000 }; // 1 minute
            },
        });

        try {
            // Get a network instance representing the channel where the smart contract is deployed.
            const network = gateway.getNetwork(this.channelName);

            // Get the smart contract from the network.
            const contract = network.getContract(this.chaincodeName);

            // Initialize a set of asset data on the ledger using the chaincode 'InitLedger' function.
            await this.initLedger(contract);

            // Return all the current assets on the ledger.
            return await this.findAll();
        } catch (e) {
            console.log(e);
            return [];
        } finally {
            gateway.close();
            client.close();
        }
    }

    async findAll(): Promise<VitalDataModel[]> {
        const client = await this.newGrpcConnection();

        const gateway = connect({
            client,
            identity: await this.newIdentity(),
            signer: await this.newSigner(),
            // Default timeouts for different gRPC calls
            evaluateOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            endorseOptions: () => {
                return { deadline: Date.now() + 15000 }; // 15 seconds
            },
            submitOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            commitStatusOptions: () => {
                return { deadline: Date.now() + 60000 }; // 1 minute
            },
        });

        try {
            // Get a network instance representing the channel where the smart contract is deployed.
            const network = gateway.getNetwork(this.channelName);

            // Get the smart contract from the network.
            const contract = network.getContract(this.chaincodeName);

            // Initialize a set of asset data on the ledger using the chaincode 'InitLedger' function.

            // Return all the current assets on the ledger.
            const resultJson = await this.getAllAssets(contract);

            const vitalData: VitalDataModel[] = resultJson.map((asset: any) => {
                return VitalDataModel.createFromChainVitalData(asset);
            });

            return vitalData;
        } catch (e) {
            console.log(e);
            return [];
        } finally {
            gateway.close();
            client.close();
        }
    }

    async findById(id: string) {
        const client = await this.newGrpcConnection();

        const gateway = connect({
            client,
            identity: await this.newIdentity(),
            signer: await this.newSigner(),
            // Default timeouts for different gRPC calls
            evaluateOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            endorseOptions: () => {
                return { deadline: Date.now() + 15000 }; // 15 seconds
            },
            submitOptions: () => {
                return { deadline: Date.now() + 5000 }; // 5 seconds
            },
            commitStatusOptions: () => {
                return { deadline: Date.now() + 60000 }; // 1 minute
            },
        });

        try {
            // Get a network instance representing the channel where the smart contract is deployed.
            const network = gateway.getNetwork(this.channelName);

            // Get the smart contract from the network.
            const contract = network.getContract(this.chaincodeName);

            // Initialize a set of asset data on the ledger using the chaincode 'InitLedger' function.

            // Return all the current assets on the ledger.
            const resultJson = await this.getAssetById(contract, id);

            return VitalDataModel.createFromChainVitalData(resultJson as unknown as IChainVitalData);
        } catch (e) {
            console.log(e);
            return [];
        } finally {
            gateway.close();
            client.close();
        }
    }
}

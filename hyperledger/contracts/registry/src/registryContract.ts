import {X509Certificate} from 'crypto';
import {Context, Contract, Info, Param, Returns, Transaction} from 'fabric-contract-api';
import {KeyEndorsementPolicy} from 'fabric-shim';
import stringify from 'json-stringify-deterministic';
import sortKeysRecursive from 'sort-keys-recursive';
import {TextDecoder} from 'util';
import {Registry} from './registry';

const utf8Decoder = new TextDecoder();

@Info({title: 'Registry', description: 'Smart contract for document registry'})
export class RegistryContract extends Contract {
    @Transaction()
    public async InitLedger(ctx: Context): Promise<void> {
        const assets: Registry[] = [
            {
                RequestID: '1',
                RequestSnapshotHash: 'hash1',
            },
        ];

        for (const asset of assets) {
            asset.docType = 'Registry';
            await ctx.stub.putState(asset.RequestID, Buffer.from(stringify(sortKeysRecursive(asset))));
            console.info(`Asset ${asset.RequestID} initialized`);
        }
    }

    @Transaction()
    @Param('assetObj', 'Registry', 'Part formed JSON of Registry')
    async CreateAsset(ctx: Context, RequestID: string, RequestSnapshotHash: string): Promise<void> {
        const asset = Registry.newInstance({RequestID, RequestSnapshotHash});

        if (await this.AssetExists(ctx, asset.RequestID)) {
            throw new Error(`The asset ${asset.RequestID} already exists`);
        }

        const assetBytes = marshal(asset);
        await ctx.stub.putState(asset.RequestID, assetBytes);

        await setEndorsingOrgs(ctx, asset.RequestID, ctx.clientIdentity.getMSPID());

        ctx.stub.setEvent('CreateAsset', assetBytes);
    }

    @Transaction(false)
    @Returns('string')
    async ReadStringAsset(ctx: Context, id: string): Promise<string> {
        const existingAssetBytes = await this.#readAsset(ctx, id);
        return existingAssetBytes.toString();
    }

    @Transaction(false)
    @Returns('Registry')
    async ReadAsset(ctx: Context, id: string): Promise<Registry> {
        const existingAssetBytes = await this.#readAsset(ctx, id);
        return Registry.newInstance(unmarshal(existingAssetBytes));
    }

    async #readAsset(ctx: Context, id: string): Promise<Uint8Array> {
        const assetBytes = await ctx.stub.getState(id);
        if (!assetBytes || assetBytes.length === 0) {
            throw new Error(`Sorry, asset ${id} has not been created`);
        }

        return assetBytes;
    }

    @Transaction()
    @Param('assetObj', 'Registry', 'Part formed JSON of Registry')
    async UpdateAsset(ctx: Context, assetUpdate: Registry): Promise<void> {
        if (assetUpdate.RequestID === undefined) {
            throw new Error('No asset ID specified');
        }

        const existingAssetBytes = await this.#readAsset(ctx, assetUpdate.RequestID);
        const existingAsset = Registry.newInstance(unmarshal(existingAssetBytes));

        if (!hasWritePermission(ctx, existingAsset)) {
            throw new Error('Only owner can update assets');
        }

        const updatedState = Object.assign({}, existingAsset, assetUpdate);
        const updatedAsset = Registry.newInstance(updatedState);

        const updatedAssetBytes = marshal(updatedAsset);
        await ctx.stub.putState(updatedAsset.RequestID, updatedAssetBytes);

        await setEndorsingOrgs(ctx, updatedAsset.RequestID, ctx.clientIdentity.getMSPID());

        ctx.stub.setEvent('UpdateAsset', updatedAssetBytes);
    }

    @Transaction()
    async MarkAsCompleted(ctx: Context, RequestID: string): Promise<void> {
        if (RequestID === undefined) {
            throw new Error('No asset ID specified');
        }

        const existingAssetBytes = await this.#readAsset(ctx, RequestID);
        const existingAssetAsString = existingAssetBytes.toString();
        const existingAsset: Registry = JSON.parse(existingAssetAsString) as Registry;

        const asset = Registry.newInstance({
            RequestID: existingAsset.RequestID,
            RequestSnapshotHash: existingAsset.RequestSnapshotHash,
        });

        const updatedState = Object.assign({}, existingAsset, asset);
        const updatedAsset = Registry.newInstance(updatedState);

        const updatedAssetBytes = marshal(updatedAsset);
        await ctx.stub.putState(updatedAsset.RequestID, updatedAssetBytes);

        await setEndorsingOrgs(ctx, updatedAsset.RequestID, ctx.clientIdentity.getMSPID());

        ctx.stub.setEvent('UpdateAsset', updatedAssetBytes);
    }

    @Transaction()
    async DeleteAsset(ctx: Context, id: string): Promise<void> {
        const assetBytes = await this.#readAsset(ctx, id);
        const asset = Registry.newInstance(unmarshal(assetBytes));

        if (!hasWritePermission(ctx, asset)) {
            throw new Error('Only owner can delete assets');
        }

        await ctx.stub.deleteState(id);

        ctx.stub.setEvent('DeleteAsset', assetBytes);
    }

    @Transaction(false)
    @Returns('boolean')
    async AssetExists(ctx: Context, id: string): Promise<boolean> {
        const assetJson = await ctx.stub.getState(id);
        return assetJson?.length > 0;
    }

    @Transaction()
    async TransferAsset(ctx: Context, id: string, newOwner: string, newOwnerOrg: string): Promise<void> {
        const assetString = await this.#readAsset(ctx, id);
        const asset = Registry.newInstance(unmarshal(assetString));

        if (!hasWritePermission(ctx, asset)) {
            throw new Error('Only owner can transfer assets');
        }

        asset.RequestSnapshotHash = toJSON(ownerIdentifier(newOwner, newOwnerOrg));

        const assetBytes = marshal(asset);
        await ctx.stub.putState(id, assetBytes);

        await setEndorsingOrgs(ctx, id, newOwnerOrg);

        ctx.stub.setEvent('TransferAsset', assetBytes);
    }

    @Transaction(false)
    @Returns('string')
    async GetAllAssets(ctx: Context): Promise<string> {
        const iterator = await ctx.stub.getStateByRange('', '');

        const assets: Registry[] = [];
        for (let result = await iterator.next(); !result.done; result = await iterator.next()) {
            const assetBytes = result.value.value;
            try {
                const asset = Registry.newInstance(unmarshal(assetBytes));
                assets.push(asset);
            } catch (err) {
                console.log(err);
            }
        }

        return marshal(assets).toString();
    }
}

function unmarshal(bytes: Uint8Array | string): object {
    const json = typeof bytes === 'string' ? bytes : utf8Decoder.decode(bytes);
    const parsed: unknown = JSON.parse(json);
    if (parsed === null || typeof parsed !== 'object') {
        throw new Error(`Invalid JSON type (${typeof parsed}): ${json}`);
    }

    return parsed;
}

function marshal(o: object): Buffer {
    return Buffer.from(toJSON(o));
}

function toJSON(o: object): string {
    return stringify(sortKeysRecursive(o));
}

interface OwnerIdentifier {
    org: string;
    user: string;
}

function hasWritePermission(ctx: Context, asset: Registry): boolean {
    // const clientId = clientIdentifier(ctx);
    // const ownerId = unmarshal(asset.RequestSnapshotHash) as OwnerIdentifier;
    // return clientId.org === ownerId.org;
    return true;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function clientIdentifier(ctx: Context, user?: string): OwnerIdentifier {
    return {
        org: ctx.clientIdentity.getMSPID(),
        user: user ?? clientCommonName(ctx),
    };
}

function clientCommonName(ctx: Context): string {
    const clientCert = new X509Certificate(ctx.clientIdentity.getIDBytes());
    const matches = clientCert.subject.match(/^CN=(.*)$/m);
    if (matches?.length !== 2) {
        throw new Error(`Unable to identify client identity common name: ${clientCert.subject}`);
    }

    return matches[1];
}

function ownerIdentifier(user: string, org: string): OwnerIdentifier {
    return {org, user};
}

async function setEndorsingOrgs(ctx: Context, ledgerKey: string, ...orgs: string[]): Promise<void> {
    const policy = newMemberPolicy(...orgs);
    await ctx.stub.setStateValidationParameter(ledgerKey, policy.getPolicy());
}

function newMemberPolicy(...orgs: string[]): KeyEndorsementPolicy {
    const policy = new KeyEndorsementPolicy();
    policy.addOrgs('MEMBER', ...orgs);
    return policy;
}

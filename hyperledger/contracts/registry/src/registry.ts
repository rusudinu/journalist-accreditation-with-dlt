import {Object as DataType, Property} from 'fabric-contract-api';

@DataType()
export class Registry {
    @Property()
    public docType?: string;

    @Property('id', 'string')
    id = ''; // basically the request ID

    @Property('requestSnapshotHash', 'string')
    requestSnapshotHash = '';

    constructor() {

    }

    static newInstance(state: Partial<Registry> = {}): Registry {
        return {
            id: assertHasValue(state.id, 'RequestID is required'),
            requestSnapshotHash: assertHasValue(state.requestSnapshotHash, 'RequestSnapshotHash is required'),
        };
    }
}

function assertHasValue<T>(value: T | undefined | null, message: string): T {
    if (value == undefined || (typeof value === 'string' && value.length === 0)) {
        throw new Error(message);
    }

    return value;
}

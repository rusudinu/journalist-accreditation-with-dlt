import {Object as DataType, Property} from 'fabric-contract-api';

@DataType()
export class Registry {
    @Property()
    public docType?: string;

    @Property('RequestID', 'string')
    RequestID = '';

    @Property('RequestID', 'string')
    RequestSnapshotHash = '';

    constructor() {

    }

    static newInstance(state: Partial<Registry> = {}): Registry {
        return {
            docType: 'Registry',
            RequestID: '',
            RequestSnapshotHash: '',
            ...state,
        };
    }
}

import {Field, ObjectType} from '@nestjs/graphql';

@ObjectType()
export class RegistryModel {
    @Field()
    RequestID: string;
    @Field()
    RequestSnapshotHash: string;

    constructor(RequestID: string, RequestSnapshotHash: string) {
        this.RequestID = RequestID;
        this.RequestSnapshotHash = RequestSnapshotHash;
    }

    // static createFromChainPrescription(chainPrescription: IChainPrescription): PrescriptionModel {
    //     return new PrescriptionModel(
    //         chainPrescription.ID,
    //         chainPrescription.Patient,
    //         chainPrescription.Issuer,
    //         chainPrescription.Medicine,
    //         chainPrescription.Completed === 'true',
    //     );
    // }
}

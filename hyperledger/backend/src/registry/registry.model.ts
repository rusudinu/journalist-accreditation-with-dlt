export class RegistryModel {
    id: string; // basically the request ID
    requestSnapshotHash: string;

    constructor(id: string, requestSnapshotHash: string) {
        this.id = id;
        this.requestSnapshotHash = requestSnapshotHash;
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

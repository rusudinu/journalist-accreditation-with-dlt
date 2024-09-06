import { Field, ObjectType } from '@nestjs/graphql';
import IChainPrescription from './chain-prescription.interface';

@ObjectType()
export class PrescriptionModel {
    @Field()
    ID: string;
    @Field()
    Patient: string;
    @Field()
    Issuer: string;
    @Field()
    Medicine: string;
    @Field()
    Completed: boolean;

    constructor(ID: string, Patient: string, Issuer: string, Medicine: string, Completed: boolean) {
        this.ID = ID;
        this.Patient = Patient;
        this.Issuer = Issuer;
        this.Medicine = Medicine;
        this.Completed = Completed;
    }

    static createFromChainPrescription(chainPrescription: IChainPrescription): PrescriptionModel {
        return new PrescriptionModel(
            chainPrescription.ID,
            chainPrescription.Patient,
            chainPrescription.Issuer,
            chainPrescription.Medicine,
            chainPrescription.Completed === 'true',
        );
    }
}

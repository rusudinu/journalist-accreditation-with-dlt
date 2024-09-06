import { Field, ObjectType } from '@nestjs/graphql';
import IChainPrescription from './chain-prescription.interface';

@ObjectType()
export class MyPrescriptionModel {
    @Field()
    ID: string;
    @Field()
    Patient: string;
    @Field()
    Issuer: string;
    @Field()
    Medicine: string;
    @Field()
    type: 'issued' | 'received';

    constructor(ID: string, Patient: string, Issuer: string, Medicine: string, type: 'issued' | 'received') {
        this.ID = ID;
        this.Patient = Patient;
        this.Issuer = Issuer;
        this.Medicine = Medicine;
        this.type = type;
    }

    static createFromChainPrescription(chainPrescription: IChainPrescription, type: 'issued' | 'received'): MyPrescriptionModel {
        return new MyPrescriptionModel(
            chainPrescription.ID,
            chainPrescription.Patient,
            chainPrescription.Issuer,
            chainPrescription.Medicine,
            type,
        );
    }
}

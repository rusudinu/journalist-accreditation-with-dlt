import { Field, InputType } from '@nestjs/graphql';

@InputType()
export class CreatePrescriptionInput {
    @Field()
    ID: string;
    @Field()
    Patient: string;
    @Field()
    Issuer: string;
    @Field()
    Medicine: string;

    constructor(ID: string, Patient: string, Issuer: string, Medicine: string) {
        this.ID = ID;
        this.Patient = Patient;
        this.Issuer = Issuer;
        this.Medicine = Medicine;
    }
}

import { Field, ObjectType } from '@nestjs/graphql';
import IChainVitalData from './chain-vital-data.interface';

@ObjectType()
export class VitalDataModel {
    @Field()
    ID: string;
    @Field()
    patient: string;
    @Field()
    name: string;
    @Field()
    dateOfBirth: string;
    @Field()
    bloodType: string;
    @Field()
    nameOfGPSurgery: string;
    @Field()
    badReactionsToDrugs: string;
    @Field()
    currentMedications: string;
    @Field()
    currentIllnesses: string;

    constructor(
        ID: string,
        patient: string,
        name: string,
        dateOfBirth: string,
        bloodType: string,
        nameOfGPSurgery: string,
        badReactionsToDrugs: string,
        currentMedications: string,
        currentIllnesses: string,
    ) {
        this.ID = ID;
        this.patient = patient;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.nameOfGPSurgery = nameOfGPSurgery;
        this.badReactionsToDrugs = badReactionsToDrugs;
        this.currentMedications = currentMedications;
        this.currentIllnesses = currentIllnesses;
    }

    static createFromChainVitalData(chainPrescription: IChainVitalData): VitalDataModel {
        return new VitalDataModel(
            chainPrescription.ID,
            chainPrescription.patient,
            chainPrescription.name,
            chainPrescription.dateOfBirth,
            chainPrescription.bloodType,
            chainPrescription.nameOfGPSurgery,
            chainPrescription.badReactionsToDrugs,
            chainPrescription.currentMedications,
            chainPrescription.currentIllnesses,
        );
    }
}

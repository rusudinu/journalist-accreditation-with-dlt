import { Resolver, Query, Mutation, Args } from '@nestjs/graphql';
import { Logger } from 'nestjs-pino';
import { UserService } from '../user/user.service';
import { CreatePrescriptionInput } from './create-prescription.input';
import { PrescriptionModel } from './prescription.model';
import { PrescriptionService } from './prescription.service';

@Resolver(() => PrescriptionModel)
export class PrescriptionResolver {
    constructor(
        private readonly prescriptionService: PrescriptionService,
        private readonly userService: UserService,
        private readonly logger: Logger,
    ) {}

    @Query(() => [PrescriptionModel])
    async getAllPrescriptions(): Promise<PrescriptionModel[]> {
        this.logger.log('Fetching all prescriptions');
        return this.prescriptionService.findAll();
    }

    @Query(() => PrescriptionModel)
    async getPrescriptionById(@Args('id') id: string): Promise<PrescriptionModel | never[]> {
        this.logger.log('Fetching prescription by id');
        const prescription = await this.prescriptionService.findById(id);
        if (!prescription || (prescription instanceof Array && prescription.length === 0)) {
            this.logger.log('Prescription data not found.');
            throw new Error('Prescription data not found.');
        }
        return prescription;
    }

    @Mutation(() => PrescriptionModel)
    async markPrescriptionAsCompleted(@Args('id') id: string): Promise<PrescriptionModel | never[]> {
        this.logger.debug('Marking prescription as completed');
        this.logger.log('Fetching prescription by id');
        const prescription = await this.prescriptionService.findById(id);
        if (!prescription || (prescription instanceof Array && prescription.length === 0)) {
            this.logger.log('Prescription data not found.');
            throw new Error('Prescription data not found.');
        }
        await this.prescriptionService.markPrescriptionAsCompleted(id);
        return this.prescriptionService.findById(id);
    }

    @Mutation(() => PrescriptionModel)
    async createPrescription(@Args('input') input: CreatePrescriptionInput): Promise<PrescriptionModel | never[]> {
        this.logger.debug('Creating vital data');
        await this.userService.linkPrescriptionToUser(input.Patient, input.ID);
        await this.userService.linkedIssuedPrescriptionToUser(input.Issuer, input.ID);
        await this.prescriptionService.createPrescription(input);
        return this.prescriptionService.findById(input.ID);
    }

    @Mutation(() => PrescriptionModel)
    async updatePrescription(@Args('input') input: CreatePrescriptionInput): Promise<PrescriptionModel | never[]> {
        this.logger.debug('Updating vital data');
        await this.prescriptionService.updatePrescription(input);
        return this.prescriptionService.findById(input.ID);
    }
}

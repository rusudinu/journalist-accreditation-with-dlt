import { Resolver, Query } from '@nestjs/graphql';
import { AuthenticatedUser } from 'nest-keycloak-connect';
import { Logger } from 'nestjs-pino';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';
import { MyPrescriptionModel } from './my-prescription.model';
import { PrescriptionModel } from './prescription.model';
import { PrescriptionService } from './prescription.service';

@Resolver(() => MyPrescriptionModel)
export class MyPrescriptionResolver {
    constructor(
        private readonly prescriptionService: PrescriptionService,
        private readonly userService: UserService,
        private readonly logger: Logger,
    ) {}

    @Query(() => [MyPrescriptionModel])
    async myPrescriptions(@AuthenticatedUser() user: any): Promise<MyPrescriptionModel[]> {
        let dbUser = await this.userService.findOne(user.sub);
        if (!user) {
            return [];
        }
        dbUser = dbUser as User;
        const prescriptions: MyPrescriptionModel[] = [];
        const issuedPrescriptionIds = dbUser.issuedPrescriptions?.split(',') ?? [];
        const receivedPrescriptionIds = dbUser.prescriptions?.split(',') || [];

        for (const issuedPrId of issuedPrescriptionIds) {
            this.logger.debug(`Fetching issued prescription with ID: ${issuedPrId}`);
            try {
                let prescription = await this.prescriptionService.findById(issuedPrId);
                if (prescription) {
                    prescription = prescription as PrescriptionModel;
                    this.logger.debug(`Fetched issued prescription with ID: ${prescription.ID}`);
                    if (prescription.ID === undefined) {
                        this.logger.debug('Prescription ID is undefined.. skipping prescription');
                        continue;
                    }
                    prescriptions.push(
                        new MyPrescriptionModel(
                            prescription.ID,
                            prescription.Patient,
                            prescription.Issuer,
                            prescription.Medicine,
                            'issued',
                        ),
                    );
                }
            } catch (e) {
                this.logger.error(e);
            }
        }

        for (const receivedPrId of receivedPrescriptionIds) {
            this.logger.debug(`Fetching received prescription with ID: ${receivedPrId}`);
            try {
                let prescription = await this.prescriptionService.findById(receivedPrId);
                if (prescription) {
                    prescription = prescription as PrescriptionModel;
                    this.logger.debug(`Received prescription: ${JSON.stringify(prescription)}`);
                    if (prescription.ID === undefined) {
                        this.logger.debug('Prescription ID is undefined.. skipping prescription');
                        continue;
                    }
                    prescriptions.push(
                        new MyPrescriptionModel(
                            prescription.ID,
                            prescription.Patient,
                            prescription.Issuer,
                            prescription.Medicine,
                            'received',
                        ),
                    );
                }
            } catch (e) {
                this.logger.error(e);
            }
        }

        return prescriptions;
    }
}

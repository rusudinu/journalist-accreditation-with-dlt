import { Resolver, Mutation } from '@nestjs/graphql';
import { Logger } from 'nestjs-pino';
import { forkJoin, lastValueFrom } from 'rxjs';
import { PrescriptionService } from '../prescription/prescription.service';
import { VitalDataService } from '../vital-data/vital-data.service';
import { ChainUtilsModel } from './chain-utils.model';

@Resolver(() => ChainUtilsModel)
export class ChainUtilsResolver {
    constructor(
        private readonly prescriptionService: PrescriptionService,
        private readonly vitalDataService: VitalDataService,
        private readonly logger: Logger,
    ) {}

    @Mutation(() => ChainUtilsModel)
    async initEntireChain(): Promise<ChainUtilsModel> {
        this.logger.log('Initializing chain');
        const vitalData = this.vitalDataService.initChain();
        const prescription = this.prescriptionService.initChain();

        return lastValueFrom(forkJoin([vitalData, prescription])).then(() => {
            return new ChainUtilsModel('Chain initialized');
        });
    }
}

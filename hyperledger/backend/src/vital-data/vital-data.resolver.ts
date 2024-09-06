import { Resolver, Query, Args, Mutation } from '@nestjs/graphql';
import { Logger } from 'nestjs-pino';
import { CreateVitalDataInput } from './create-vital-data.input';
import { VitalDataModel } from './vital-data.model';
import { VitalDataService } from './vital-data.service';

@Resolver(() => VitalDataModel)
export class VitalDataResolver {
    constructor(
        private readonly vitalDataService: VitalDataService,
        private readonly logger: Logger,
    ) {}

    @Query(() => [VitalDataModel])
    async getAllVitalData(): Promise<VitalDataModel[]> {
        this.logger.log('Fetching all prescriptions');
        return this.vitalDataService.findAll();
    }

    @Query(() => VitalDataModel)
    async getVitalDataById(@Args('id') id: string): Promise<VitalDataModel | never[]> {
        this.logger.log('Fetching vital data by id');
        const vitalData = await this.vitalDataService.findById(id);
        if (!vitalData || (vitalData instanceof Array && vitalData.length === 0)) {
            this.logger.log('Vital data not found.');
            throw new Error('Vital data not found.');
        }
        return vitalData;
    }

    @Mutation(() => VitalDataModel)
    async createVitalData(@Args('input') input: CreateVitalDataInput): Promise<VitalDataModel> {
        this.logger.debug('Creating vital data');
        return this.vitalDataService.createVitalData(input);
    }

    @Mutation(() => VitalDataModel)
    async updateVitalData(@Args('input') input: CreateVitalDataInput): Promise<VitalDataModel> {
        this.logger.debug('Updating vital data');
        return this.vitalDataService.updateVitalData(input);
    }
}

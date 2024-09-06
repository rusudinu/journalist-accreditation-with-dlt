import { Module } from '@nestjs/common';
import { PrescriptionService } from '../prescription/prescription.service';
import { VitalDataService } from '../vital-data/vital-data.service';
import { ChainUtilsResolver } from './chain-utils.resolver';

@Module({
    providers: [PrescriptionService, VitalDataService, ChainUtilsResolver],
})
export class ChainUtilsModule {}

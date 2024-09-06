import { Module } from '@nestjs/common';
import { VitalDataService } from '../vital-data/vital-data.service';
import { VitalDataResolver } from './vital-data.resolver';

@Module({
    providers: [VitalDataService, VitalDataResolver],
})
export class VitalDataModule {}

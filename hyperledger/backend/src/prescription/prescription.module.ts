import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';
import { MyPrescriptionResolver } from './my-prescription.resolver';
import { PrescriptionResolver } from './prescription.resolver';
import { PrescriptionService } from './prescription.service';

@Module({
    imports: [TypeOrmModule.forFeature([User])],
    providers: [UserService, PrescriptionService, PrescriptionResolver, MyPrescriptionResolver],
})
export class PrescriptionModule {}

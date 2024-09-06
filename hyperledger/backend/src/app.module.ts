import {join} from 'path';
import {MiddlewareConsumer, Module} from '@nestjs/common';
import {MongooseModule} from '@nestjs/mongoose';
import {ChainUtilsModule} from './chain-utils/chain-utils.module';
import {GraphQLModule} from '@nestjs/graphql';
import {ApolloDriver, ApolloDriverConfig} from '@nestjs/apollo';
import {OpenTelemetryMiddleware} from './open.telemetry.middleware';
import {LoggerModule} from 'nestjs-pino';
import {logger} from './logger';
import {PrescriptionModule} from './prescription/prescription.module';
import {VitalDataModule} from './vital-data/vital-data.module';

@Module({
    imports: [
        LoggerModule.forRoot({
            pinoHttp: {
                // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
                logger: logger,
                autoLogging: false,
                quietReqLogger: true,
            },
        }),
        MongooseModule.forRoot('mongodb://localhost/nest'),
        GraphQLModule.forRoot<ApolloDriverConfig>({
            driver: ApolloDriver,
            // autoSchemaFile: true, // in memory
            autoSchemaFile: join(process.cwd(), 'src/schema.gql'),
            sortSchema: true,
        }),
        PrescriptionModule,
        ChainUtilsModule,
        VitalDataModule,
    ],
    controllers: [],
})
export class AppModule {
    configure(consumer: MiddlewareConsumer): void {
        consumer.apply(OpenTelemetryMiddleware).forRoutes('*');
    }
}

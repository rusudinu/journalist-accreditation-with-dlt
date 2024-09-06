import {MiddlewareConsumer, Module} from '@nestjs/common';
import {OpenTelemetryMiddleware} from './open.telemetry.middleware';
import {LoggerModule} from 'nestjs-pino';
import {logger} from './logger';
import {RegistryModule} from './registry/registry.module';

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
        RegistryModule,
    ],
    controllers: [],
})
export class AppModule {
    configure(consumer: MiddlewareConsumer): void {
        consumer.apply(OpenTelemetryMiddleware).forRoutes('*');
    }
}

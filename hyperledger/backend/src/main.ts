import {NestFactory} from '@nestjs/core';
import {AppModule} from './app.module';
import otelSDK from './tracer';
import {install} from 'source-map-support';
import {Logger} from 'nestjs-pino';

install();

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function bootstrap() {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access,@typescript-eslint/no-unsafe-call
    otelSDK.start();
    const app = await NestFactory.create(AppModule);
    app.useLogger(app.get(Logger));
    app.enableCors({
        origin: ['http://localhost:5173', 'http://localhost:4200', 'http://localhost:3000'],
    });
    await app.listen(3000);
}

// eslint-disable-next-line @typescript-eslint/no-floating-promises
bootstrap().then((r) => r);

import { Injectable, NestMiddleware } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';
import { context, trace, SpanKind } from '@opentelemetry/api';

@Injectable()
export class OpenTelemetryMiddleware implements NestMiddleware {
    use(req: Request, res: Response, next: NextFunction): void {
        const tracer = trace.getTracer('nestjs-lambda');

        const span = tracer.startSpan(req.path, {
            kind: SpanKind.SERVER,
        });

        context.with(trace.setSpan(context.active(), span), () => {
            res.header('Traceid', span.spanContext().traceId);
            res.header('Access-Control-Expose-Headers', 'Traceid');
            next();
            span.end();
        });
    }
}

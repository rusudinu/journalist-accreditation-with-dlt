import Pino, { LoggerOptions } from 'pino';
import { trace, context } from '@opentelemetry/api';

export const loggerOptions: LoggerOptions = {
    level: 'debug',
    formatters: {
        level(label) {
            return { level: label };
        },
        log(object) {
            const span = trace.getActiveSpan();
            if (!span) return { ...object };
            // @ts-expect-error spanContext is private
            // eslint-disable-next-line no-unsafe-optional-chaining
            const { spanId, traceId } = trace.getSpan(context.active())?.spanContext();
            // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
            return { ...object, spanId, traceId };
        },
    },
};

export const logger: any = Pino(loggerOptions);

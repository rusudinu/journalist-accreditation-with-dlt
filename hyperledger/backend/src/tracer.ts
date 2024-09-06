import { SimpleSpanProcessor } from '@opentelemetry/sdk-trace-base';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
import { getNodeAutoInstrumentations } from '@opentelemetry/auto-instrumentations-node';
import { NodeSDK } from '@opentelemetry/sdk-node';

// eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
const traceExporter = new OTLPTraceExporter();
// eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
const spanProcessor: any = new SimpleSpanProcessor(traceExporter);
// eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
const otelSDK = new NodeSDK({
    spanProcessor,
    traceExporter,
    instrumentations: [getNodeAutoInstrumentations()],
});

export default otelSDK;

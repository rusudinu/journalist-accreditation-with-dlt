package com.rusudinu.backend.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer zonedDateTimeCustomizer() {

        return builder -> {
            builder.serializers(new StdSerializer<>(ZonedDateTime.class) {
                private static final long serialVersionUID = 7267001379918924374L;

                @Override
                public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(value.toString());
                }
            });
            builder.deserializers(new StdDeserializer<>(ZonedDateTime.class) {
                private static final long serialVersionUID = 154543596456722486L;

                @Override
                public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                    double timestamp = p.getDoubleValue();
                    long seconds = (long) timestamp;
                    long nanos = (long) ((timestamp - seconds) * 1_000_000_000);
                    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneId.systemDefault());
                }
            });
        };
    }
}

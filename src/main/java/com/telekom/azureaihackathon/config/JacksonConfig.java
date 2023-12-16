package com.telekom.azureaihackathon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class JacksonConfig {

    @Bean
    public ExchangeStrategies strategies(ObjectMapper objectMapper) {
        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(objectMapper);
        Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(objectMapper);
        return ExchangeStrategies.builder()
            .codecs(clientCodecConfigurer -> {
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonDecoder(decoder);
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonEncoder(encoder);
            })
            .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(INDENT_OUTPUT);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        objectMapper.setSerializationInclusion(NON_NULL);
        return objectMapper;
    }
}

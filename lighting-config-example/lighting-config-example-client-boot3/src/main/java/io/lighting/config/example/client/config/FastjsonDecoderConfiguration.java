package io.lighting.config.example.client.config;

import com.alibaba.fastjson2.JSONObject;
import io.lighting.config.client.value.ConfigValueDecoder;
import io.lighting.config.core.model.ContentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;

/**
 * Demonstrates how to plug a custom ConfigValueDecoder for Fastjson's JSONObject.
 */
@Configuration
public class FastjsonDecoderConfiguration {

    @Bean
    public ConfigValueDecoder fastjsonValueDecoder() {
        return new FastjsonJsonObjectDecoder();
    }

    static class FastjsonJsonObjectDecoder implements ConfigValueDecoder {

        @Override
        public boolean supports(ContentType contentType, Type targetType) {
            if (targetType instanceof Class<?>) {
                return JSONObject.class.isAssignableFrom((Class<?>) targetType);
            }
            return false;
        }

        @Override
        public Object decode(String rawValue, ContentType contentType, Type targetType) {
            if (rawValue == null || rawValue.isEmpty()) {
                return null;
            }
            return JSONObject.parse(rawValue);
        }
    }
}

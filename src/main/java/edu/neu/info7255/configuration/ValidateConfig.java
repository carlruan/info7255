package edu.neu.info7255.configuration;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidateConfig {
    @Value("${jsonSchema.location}")
    private String schemaLocation;
    @Bean
    public JsonSchema getJsonSchemaFactory(){
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(ValidateConfig.class.getResourceAsStream(schemaLocation));
    }
}

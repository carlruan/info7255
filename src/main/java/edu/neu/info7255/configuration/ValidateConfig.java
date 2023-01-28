package edu.neu.info7255.configuration;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ValidateConfig {
    @Bean
    public JsonSchema getJsonSchemaFactory(){
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchema jsonSchema = factory
                .getSchema(ValidateConfig.class.getResourceAsStream("/json/jsonSchema.json"));
        return jsonSchema;
    }
}

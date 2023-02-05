package edu.neu.info7255.configuration;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Scanner;

@Configuration
public class ValidateConfig {
    @Value("${jsonSchema.location}")
    private String schemaLocation;
    @Bean
    public JsonSchema getJsonSchemaFactory(){
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(ValidateConfig.class.getResourceAsStream(schemaLocation));
    }

    @Bean
    public JSONObject getJsonSchemaJSONObject(){
        String json = new Scanner(ValidateConfig.class.getResourceAsStream(schemaLocation), "UTF-8").useDelimiter("\\A").next();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (JSONObject)jsonObject.get("properties");
    }
}

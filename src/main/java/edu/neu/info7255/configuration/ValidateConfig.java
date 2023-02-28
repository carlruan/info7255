package edu.neu.info7255.configuration;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Scanner;

@Configuration
public class ValidateConfig {
    @Bean
    public RSAKey generateRSAKey() throws JOSEException {
        return new RSAKeyGenerator(2048)
                .keyID("info7255")
                .generate();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Value("${jsonSchema.location}")
    private String schemaLocation;
    @Value("${jsonSchema.patch}")
    private String patchSchemaLocation;
    @Bean(name = "jsonSchema")
    public JsonSchema getJsonSchemaFactory(){
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        return factory.getSchema(ValidateConfig.class.getResourceAsStream(schemaLocation));
    }
    @Bean(name = "patchSchema")
    public JsonSchema getPatchSchemaFactory(){
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        return factory.getSchema(ValidateConfig.class.getResourceAsStream(patchSchemaLocation));
    }

    @Bean(name = "jsonSchemaJSONObject")
    public JSONObject getJsonSchemaJSONObject(){
        return getJSONObjectByLocation(schemaLocation);
    }
    @Bean(name = "patchSchemaJSONObject")
    public JSONObject getPatchSchemaJSONObject(){
        return getJSONObjectByLocation(patchSchemaLocation);
    }

    public JSONObject getJSONObjectByLocation(String location){
        String json = new Scanner(ValidateConfig.class.getResourceAsStream(location), "UTF-8").useDelimiter("\\A").next();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (JSONObject)jsonObject.get("properties");
    }

}

package edu.neu.info7255.configuration;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import edu.neu.info7255.util.JsonValidateUtil;
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
    public JsonSchema planSchemaFactory(){
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema jsonSchema = factory.getSchema(ValidateConfig.class.getResourceAsStream(schemaLocation));
        JsonValidateUtil.schemaMap.put("plan", jsonSchema);
        return jsonSchema;
    }
    @Bean(name = "patchSchema")
    public JsonSchema patchPlanSchemaFactory(){
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema jsonSchema = factory.getSchema(ValidateConfig.class.getResourceAsStream(patchSchemaLocation));
        JsonValidateUtil.schemaMap.put("patchplan", jsonSchema);
        return jsonSchema;
    }

    @Bean(name = "planJSONObject")
    public JSONObject planJSONObject(){
        return getJSONObjectByLocation(schemaLocation, "plan");
    }
    @Bean(name = "patchPlanJSONObject")
    public JSONObject patchPlanJSONObject(){
        return getJSONObjectByLocation(patchSchemaLocation, "patchplan");
    }

    public JSONObject getJSONObjectByLocation(String location, String type){
        String json = new Scanner(ValidateConfig.class.getResourceAsStream(location), "UTF-8").useDelimiter("\\A").next();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject res = (JSONObject)jsonObject.get("properties");
        JsonValidateUtil.jsonObjectMap.put(type, res);
        return res;
    }

}

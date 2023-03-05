package edu.neu.info7255.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import edu.neu.info7255.exception.CustomException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class JsonValidateUtil {

    public static Map<String, JsonSchema> schemaMap = new HashMap<>();
    public static Map<String, JSONObject> jsonObjectMap = new HashMap<>();

    public JSONObject validateJsonSchema(String requestJsonString, String type)  {
        JsonSchema jsonSchema = schemaMap.get(type);
        if(jsonSchema == null){
            throw new CustomException("Unsupported object type: " + type, HttpStatus.BAD_REQUEST);
        }
        Set<ValidationMessage> errors;
        try {
            errors = jsonSchema.validate(new ObjectMapper().readTree(requestJsonString));
        } catch (JsonProcessingException e) {
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        if(!errors.isEmpty()){
            StringBuilder err = new StringBuilder();
            errors.forEach(error -> err.append(error.getMessage()).append("; "));
            throw new CustomException(err.toString(), HttpStatus.BAD_REQUEST);
        }
        // Parse Json
        JSONParser parser = new JSONParser();
        JSONObject json;
        try {
            json = (JSONObject) parser.parse(requestJsonString);
        } catch (ParseException e) {
            throw new CustomException(e.getPosition()+", " + e, HttpStatus.BAD_REQUEST);
        }
        if(type.length() > 5 && type.startsWith("patch")){
            type = type.substring(5);
        }
        String objectType = (String)json.get("objectType");
        if(objectType == null || !type.equals(objectType)){
            throw new CustomException("Json objectType should match with url type!", HttpStatus.BAD_REQUEST);
        }
        return json;
    }
}

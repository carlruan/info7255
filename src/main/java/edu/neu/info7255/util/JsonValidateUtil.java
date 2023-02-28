package edu.neu.info7255.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import edu.neu.info7255.exception.CustomException;
import edu.neu.info7255.service.PlanService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JsonValidateUtil {

    public JSONObject validateJsonSchema(String requestJsonString, JsonSchema jsonSchema) throws JsonProcessingException, ParseException {
        Set<ValidationMessage> errors;
        errors = jsonSchema.validate(new ObjectMapper().readTree(requestJsonString));
        if(!errors.isEmpty()){
            StringBuilder err = new StringBuilder();
            errors.forEach(error -> err.append(error.getMessage()).append("; "));
            throw new CustomException(err.toString(), HttpStatus.BAD_REQUEST);
        }
        // Parse Json
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(requestJsonString);
        return json;
    }
}

package edu.neu.info7255.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import edu.neu.info7255.exception.CustomException;
import edu.neu.info7255.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/v2/plan")
@Slf4j
public class MedicalPlanControllerV2 {

    private final JsonSchema jsonSchema;
    private final PlanService planService;
    private final JSONObject jsonSchemaJsonObject;

    @Autowired
    public MedicalPlanControllerV2(JsonSchema jsonSchema, PlanService planService, JSONObject jsonObject, JSONObject jsonSchemaJsonObject){
        this.jsonSchema = jsonSchema;
        this.planService = planService;
        this.jsonSchemaJsonObject = jsonSchemaJsonObject;
    }
    @PostMapping()
    public ResponseEntity<HashMap<String, String>> createPlan(@RequestBody String resquestJsonString){
        // Json Schema validation
        Set<ValidationMessage> errors;
        try {
            errors = jsonSchema.validate(new ObjectMapper().readTree(resquestJsonString));
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
        try{
            json = (JSONObject) parser.parse(resquestJsonString);
        }catch(ParseException e) {
            throw new CustomException(e.getPosition()+", " + e, HttpStatus.BAD_REQUEST);
        }
        String objectId = (String)json.get("objectId");
        String key = "plan:" + objectId;
        if(planService.isPresent(key)) throw new CustomException("Medical plan with id: " + key + " already exists!", HttpStatus.CONFLICT);
        String etag = planService.save(json, key);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .eTag(etag)
                .body(new HashMap<>() {{
                    put("objectId", objectId);
                }});
    }

    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getPlan(@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String etag,
                                              @PathVariable(value = "id") String id) throws IOException {
        if(id == null || id.length() == 0) throw new CustomException("Invalid input!", HttpStatus.BAD_REQUEST);
        id = "plan:" + id;
        if(!planService.isPresent(id)) throw new CustomException("Medical plan with id: " + id + " cannot be found!", HttpStatus.NOT_FOUND);
        String curEtag = planService.getEtag(id);
        if(etag != null && etag.length() != 0 && curEtag.equals(etag)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).body(null);
        }else{
            return ResponseEntity.status(HttpStatus.OK).eTag(curEtag).body(planService.getByIdWithJsonSchema(id, jsonSchemaJsonObject));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePlan(@PathVariable(value = "id") String id){
        if(id == null || id.length() == 0) throw new CustomException("Medical plan id cannot be null!", HttpStatus.BAD_REQUEST);
        if(!planService.isPresent("plan:" + id)) throw new CustomException("Medical plan with id: " + id + " cannot be found!", HttpStatus.NOT_FOUND);
        planService.deleteById("plan:"+id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }


    @GetMapping("/test")
    public ResponseEntity<Object> test(){
        JSONObject result = planService.getByIdWithJsonSchema("plan:12xvxc345ssdsds-508", jsonSchemaJsonObject);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

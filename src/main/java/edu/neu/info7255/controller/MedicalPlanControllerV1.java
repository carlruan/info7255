package edu.neu.info7255.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import edu.neu.info7255.exception.CustomException;
import edu.neu.info7255.service.EtagService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/v1/plan")
@Slf4j
public class MedicalPlanControllerV1 {
    private final JsonSchema jsonSchema;
    private final EtagService etagService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    public MedicalPlanControllerV1(JsonSchema jsonSchema, EtagService etagService, RedisTemplate<String, Object> redisTemplate){
        this.jsonSchema = jsonSchema;
        this.etagService = etagService;
        this.redisTemplate = redisTemplate;
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
        String hashString = etagService.generateHash(json.toJSONString());
        redisTemplate.opsForValue().set(objectId, json.toJSONString());
        redisTemplate.opsForValue().set(objectId + ":hash", hashString);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .eTag(hashString)
                .body(new HashMap<>() {
                    {
                        put("objectId", objectId);
                    }
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPlan(@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String etag,
                                              @PathVariable(value = "id") String id){
        if(id == null || id.length() == 0) throw new CustomException("Invalid input!", HttpStatus.BAD_REQUEST);
        String storedHash = (String)redisTemplate.opsForValue().get(id + ":hash");
        if(storedHash == null || storedHash.length() == 0) throw new CustomException("Medical plan with id: " + id + " cannot be found!", HttpStatus.NOT_FOUND);
        if(etag != null && etag.length() != 0 && storedHash.equals(etag)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).body(null);
        }else{
            String storedString = (String)redisTemplate.opsForValue().get(id);
            JSONParser parser = new JSONParser();
            JSONObject json;
            try{
                json = (JSONObject) parser.parse(storedString);
            }catch(ParseException e) {
                throw new CustomException(e.getPosition()+", " + e, HttpStatus.BAD_REQUEST);
            }
            return ResponseEntity.status(HttpStatus.OK).eTag(storedHash).body(json);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePlan(@PathVariable(value = "id") String id){
        if(id == null || id.length() == 0) throw new CustomException("Medical plan id cannot be null!", HttpStatus.BAD_REQUEST);
        String storedString = (String)redisTemplate.opsForValue().get(id);
        if(storedString == null || storedString.length() == 0) throw new CustomException("Medical plan with id: " + id + " cannot be found!", HttpStatus.NOT_FOUND);
        redisTemplate.delete(id);
        redisTemplate.delete(id + ":hash");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

}

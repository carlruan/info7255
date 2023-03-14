package edu.neu.info7255.controller;

import edu.neu.info7255.exception.CustomException;
import edu.neu.info7255.service.RedisService;
import edu.neu.info7255.util.JsonValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

@RestController
@RequestMapping("/v2")
@Slf4j
@Validated
public class MedicalPlanControllerV2 {

    private final RedisService redisService;
    private JsonValidateUtil jsonValidateUtil;

    @Autowired
    public MedicalPlanControllerV2(RedisService redisService,
                                   JsonValidateUtil jsonValidateUtil){
        this.redisService = redisService;
        this.jsonValidateUtil = jsonValidateUtil;
    }
    @PostMapping("/{type}")
    public ResponseEntity<HashMap<String, String>> createPlan(@PathVariable(value = "type") @NotBlank String type,
                                                              @RequestBody @NotBlank String requestJsonString){

        // Json Schema validation
        JSONObject json = jsonValidateUtil.validateJsonSchema(requestJsonString, type);
        String objectId = (String)json.get("objectId");
        String key = type + ":" + objectId;
        if(redisService.isPresent(key)) throw new CustomException("Object type: " + type + " with id: " + key + " already exists!", HttpStatus.CONFLICT);
        String etag = redisService.save(json, key);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .eTag(etag)
                .body(new HashMap<>() {{
                    put("object_id", objectId);
                }});
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<JSONObject> getPlan(@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String etag,
                                              @PathVariable(value = "type") @NotBlank String type,
                                              @PathVariable(value = "id") @NotBlank String id) {
        id = type + ":" + id;
        if(!redisService.isPresent(id)) throw new CustomException(id + " cannot be found!", HttpStatus.NOT_FOUND);
        String curEtag = redisService.getEtag(id);
        if(etag != null && !etag.isBlank() && curEtag.equals(etag)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(curEtag).body(null);
        }else{
            JSONObject jsonObject = JsonValidateUtil.jsonObjectMap.get(type);
            if(jsonObject == null){
                throw new CustomException("Unsupported object type: " + type, HttpStatus.BAD_REQUEST);
            }
            return ResponseEntity.status(HttpStatus.OK).eTag(curEtag).body(redisService.getByIdWithJsonSchema(id, jsonObject));
        }
    }

    @PatchMapping("/{type}/{id}")
    public ResponseEntity<JSONObject> patchPlan(@RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag,
                                                @PathVariable(value = "type") @NotBlank String type,
                                                @PathVariable(value = "id") @NotNull String id,
                                                @RequestBody @NotBlank String requestJsonString){
        if(etag == null || etag.isBlank()) throw new CustomException("If-Match etag cannot be null or empty!", HttpStatus.BAD_REQUEST);
        JSONObject jsonObject = jsonValidateUtil.validateJsonSchema(requestJsonString, "patch" + type);
        id = type+ ":" + id;
        if(!redisService.isPresent(id)) throw new CustomException(id + " cannot be found!", HttpStatus.NOT_FOUND);
        String curEtag = redisService.getEtag(id);
        if(curEtag.equals(etag)){
            // Json Schema validation
            String newEtag = redisService.save(jsonObject, id);
            return ResponseEntity.status(HttpStatus.CREATED).eTag(newEtag).body(null);
        }else{
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).eTag(curEtag).body(null);
        }

    }

    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Object> deletePlan(@PathVariable(value = "type") @NotBlank String type,
                                             @PathVariable(value = "id") @NotBlank String id){
        id = type + ":" + id;
        if(!redisService.isPresent(id)) throw new CustomException(id + " cannot be found!", HttpStatus.NOT_FOUND);
        JSONObject jsonObject = JsonValidateUtil.jsonObjectMap.get(type);
        if(jsonObject == null){
            throw new CustomException("Unsupported object type: " + type, HttpStatus.BAD_REQUEST);
        }
        redisService.deleteByIdWithJsonSchema(id, jsonObject);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

}

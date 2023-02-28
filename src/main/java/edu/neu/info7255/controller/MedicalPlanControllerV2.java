package edu.neu.info7255.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.JsonSchema;
import edu.neu.info7255.exception.CustomException;
import edu.neu.info7255.service.PlanService;
import edu.neu.info7255.util.JsonValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

@RestController
@RequestMapping("/v2/plan")
@Slf4j
@Validated
public class MedicalPlanControllerV2 {

    private final JsonSchema jsonSchema;
    private final JsonSchema patchSchema;
    private final PlanService planService;
    private final JSONObject jsonSchemaJsonObject;
    private final JSONObject patchSchemaJsonObject;
    private JsonValidateUtil jsonValidateUtil;

    @Autowired
    public MedicalPlanControllerV2(@Qualifier("jsonSchema") JsonSchema jsonSchema,
                                   @Qualifier("patchSchema")JsonSchema patchSchema,
                                   PlanService planService,
                                   @Qualifier("jsonSchemaJSONObject")JSONObject jsonSchemaJsonObject,
                                   @Qualifier("patchSchemaJSONObject")JSONObject patchSchemaJsonObject,
                                   JsonValidateUtil jsonValidateUtil){
        this.jsonSchema = jsonSchema;
        this.patchSchema = patchSchema;
        this.planService = planService;
        this.jsonSchemaJsonObject = jsonSchemaJsonObject;
        this.patchSchemaJsonObject = patchSchemaJsonObject;
        this.jsonValidateUtil = jsonValidateUtil;
    }
    @PostMapping()
    public ResponseEntity<HashMap<String, String>> createPlan(@RequestBody @NotBlank String requestJsonString){
        // Json Schema validation
        JSONObject json;
        try {
            json = jsonValidateUtil.validateJsonSchema(requestJsonString, jsonSchema);
        } catch (JsonProcessingException e) {
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ParseException e) {
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
                                              @PathVariable(value = "id") @NotNull @NotBlank String id) {
        id = "plan:" + id;
        if(!planService.isPresent(id)) throw new CustomException(id + " cannot be found!", HttpStatus.NOT_FOUND);
        String curEtag = planService.getEtag(id);
        if(etag != null && etag.length() != 0 && curEtag.equals(etag)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(curEtag).body(null);
        }else{
            return ResponseEntity.status(HttpStatus.OK).eTag(curEtag).body(planService.getByIdWithJsonSchema(id, jsonSchemaJsonObject));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JSONObject> patchPlan(@RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag,
                                                @PathVariable(value = "id") @NotNull String id,
                                                @RequestBody @NotBlank String requestJsonString){
        if(etag == null || etag.isBlank()) throw new CustomException("etag cannot be null or empty!", HttpStatus.BAD_REQUEST);
        id = "plan:" + id;
        if(!planService.isPresent(id)) throw new CustomException(id + " cannot be found!", HttpStatus.NOT_FOUND);
        String curEtag = planService.getEtag(id);
        if(curEtag.equals(etag)){
            // Json Schema validation
            JSONObject jsonObject;
            try {
                jsonObject = jsonValidateUtil.validateJsonSchema(requestJsonString, patchSchema);
            } catch (JsonProcessingException e) {
                throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (ParseException e) {
                throw new CustomException(e.getPosition()+", " + e, HttpStatus.BAD_REQUEST);
            }
            String newEtag = planService.save(jsonObject, id);
            return ResponseEntity.status(HttpStatus.CREATED).eTag(newEtag).body(null);
        }else{
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).eTag(curEtag).body(null);
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePlan(@PathVariable(value = "id") @NotBlank String id){
        id = "plan" + id;
        if(!planService.isPresent(id)) throw new CustomException(id + " cannot be found!", HttpStatus.NOT_FOUND);
        planService.deleteByIdWithJsonSchema(id, jsonSchemaJsonObject);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}

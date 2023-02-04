package edu.neu.info7255.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import edu.neu.info7255.exception.CustomException;
import edu.neu.info7255.model.MedicalPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Set;

import static org.springframework.http.RequestEntity.put;

@RestController
@RequestMapping("/plan")
@Slf4j
public class MedicalPlanController {
    @Autowired
    private JsonSchema jsonSchema;
    @Autowired
    private RedisTemplate<String, MedicalPlan> redisMedicalPlanTemplate;

    @Autowired
    private RedisTemplate<String, String> redisMd5hashTemplate;
    @PostMapping()
    public ResponseEntity<Object> createPlan(@RequestBody String resquestJsonString) throws JsonProcessingException, NoSuchAlgorithmException {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode;
        try{
            jsonNode = om.readTree(resquestJsonString);
        }catch (JsonProcessingException e){
            log.error("Parsing json error! " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        if(!errors.isEmpty()){
            StringBuilder err = new StringBuilder();
            for(ValidationMessage error : errors){
                log.error("Parsing json error! " + error.toString());
                err.append(error).append("; ");
            }
            throw new CustomException(err.toString(), HttpStatus.BAD_REQUEST);
        }
        MedicalPlan medicalPlan = om.readValue(resquestJsonString, MedicalPlan.class);
        String hashText = generateMD5Hash(medicalPlan.toString());
        log.info("Creating mediacl plan: "  + medicalPlan);
        String id = medicalPlan.getObjectId();
        redisMedicalPlanTemplate.opsForValue().set(id, medicalPlan);
        redisMd5hashTemplate.opsForValue().set(id + "-md5hash", hashText);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .eTag(hashText)
                .body(new HashMap<String, String>(){{
            put("objectId", id);
        }});
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalPlan> getPlan(@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String etag, @PathVariable(value = "id") String id){
        String md5hash = redisMd5hashTemplate.opsForValue().get(id + "-md5hash");
        MedicalPlan medicalPlan = redisMedicalPlanTemplate.opsForValue().get(id);
        if(md5hash == null || medicalPlan == null) throw new CustomException("Medical plan with id: "+ id + "does not exist!", HttpStatus.NOT_FOUND);
        if(etag == null || etag.length() == 0){
            return ResponseEntity.status(HttpStatus.OK).eTag(md5hash).body(medicalPlan);
        }
        if(etag.equals(md5hash)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(md5hash).body(null);
        }else{
            return ResponseEntity.status(HttpStatus.OK).eTag(md5hash).body(medicalPlan);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePlan(@PathVariable(value = "id") String id){
        String md5hash = redisMd5hashTemplate.opsForValue().get(id + "-md5hash");
        MedicalPlan medicalPlan = redisMedicalPlanTemplate.opsForValue().get(id);
        if(md5hash == null || medicalPlan == null) throw new CustomException("Medical plan with id: "+ id + "does not exist!", HttpStatus.NOT_FOUND);
        redisMd5hashTemplate.delete(id + "-md5hash");
        redisMedicalPlanTemplate.delete(medicalPlan.getObjectId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

//    @PatchMapping("/{id}")
//    public ResponseEntity<Object> patchPlan(@RequestHeader(HttpHeaders.IF_NONE_MATCH) String md5Hash, @PathVariable(value = "id") String id){
//
//    }

    private String generateMD5Hash(String src) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(src.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        return number.toString(16);
    }

}

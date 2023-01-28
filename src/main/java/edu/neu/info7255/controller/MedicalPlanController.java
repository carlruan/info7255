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
import java.util.Set;

@RestController
@RequestMapping("/plan")
@Slf4j
public class MedicalPlanController {
    @Autowired
    private JsonSchema jsonSchema;
    @Autowired
    private RedisTemplate<String, MedicalPlan> redisTemplate;
    @PostMapping("/{id}")
    public ResponseEntity<Object> createPlan(@RequestBody String resquestJsonString, @PathVariable(value = "id") String id) throws JsonProcessingException, NoSuchAlgorithmException {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(resquestJsonString);
        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        if(!errors.isEmpty()){
            StringBuilder err = new StringBuilder();
            for(ValidationMessage error : errors){
                log.error("Parsing json error! " + error.toString());
                err.append(error.toString()).append("; ");
            }
            throw new CustomException(err.toString(), HttpStatus.BAD_REQUEST);
//            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(err.toString());
        }
        MedicalPlan medicalPlan = om.readValue(resquestJsonString, MedicalPlan.class);
        String hashText = generateMD5Hash(resquestJsonString);
        medicalPlan.setMd5Hash(hashText);
        log.info("Parsing input String: \n" + medicalPlan);
        redisTemplate.opsForValue().set(id, medicalPlan);
        return ResponseEntity.status(HttpStatus.CREATED).eTag(hashText).body(medicalPlan);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalPlan> getPlan(@RequestHeader(HttpHeaders.IF_NONE_MATCH) String md5Hash, @PathVariable(value = "id") String id){
        MedicalPlan medicalPlan = redisTemplate.opsForValue().get(id);
        if(medicalPlan.getMd5Hash().equals(md5Hash)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(null);
        }else{
            return ResponseEntity.status(HttpStatus.OK).eTag(medicalPlan.getMd5Hash()).body(medicalPlan);
        }
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

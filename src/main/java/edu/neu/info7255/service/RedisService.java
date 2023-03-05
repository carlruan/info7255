package edu.neu.info7255.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class RedisService {
    private final EtagService etagService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    public RedisService(EtagService etagService, RedisTemplate<String, Object> redisTemplate){
        this.etagService = etagService;
        this.redisTemplate = redisTemplate;
    }
    public String save(JSONObject jsonObject, String key){
        String etag = saveEtag(jsonObject, key);
        savePlan(jsonObject);
        return etag;
    }
    private String savePlan(JSONObject jsonObject){
        String objectId = (String)jsonObject.get("objectId");
        String objectType = (String)jsonObject.get("objectType");
        String key = objectType + ":" + objectId;
        jsonObject.forEach((jsonKey, jsonValue) -> {
            if(jsonValue instanceof Number || jsonValue instanceof String){
                redisTemplate.opsForHash().put(key, jsonKey, jsonValue.toString());
            }else if(jsonValue instanceof JSONObject){
                String childKey = savePlan((JSONObject)jsonValue);
                redisTemplate.opsForSet().add(key + ":" + jsonKey, childKey);
            }else if(jsonValue instanceof JSONArray){
                ((JSONArray) jsonValue).forEach(child ->
                        redisTemplate.opsForSet().add(key + ":" + jsonKey, savePlan((JSONObject) child))
                );
            }
        });
        return key;
    }

    public JSONObject getByIdWithJsonSchema(String key, JSONObject jsonObject){
        JSONObject result = new JSONObject();
        jsonObject.keySet().forEach(jsonKey -> {
            JSONObject childObject = (JSONObject) jsonObject.get(jsonKey);
            String type = (String)childObject.get("type");
            if(type.equals("object")){
                String childKey = (String)redisTemplate.opsForSet().members(key+":"+jsonKey).iterator().next();
                result.put(jsonKey, getByIdWithJsonSchema(childKey, (JSONObject)childObject.get("properties")));
            }else if(type.equals("string")){
                result.put(jsonKey, redisTemplate.opsForHash().get(key, jsonKey));
            }else if(type.equals("integer")){
                result.put(jsonKey, Integer.parseInt((String)redisTemplate.opsForHash().get(key, jsonKey)));
            }else if(type.equals("array")){
                List<JSONObject> jsonObjects = new ArrayList<>();
                Set<Object> keys = redisTemplate.opsForSet().members(key+":"+jsonKey);
                JSONObject items = (JSONObject)((JSONArray) childObject.get("items")).get(0);
                keys.forEach(k ->
                    jsonObjects.add(getByIdWithJsonSchema((String)k, (JSONObject) items.get("properties")))
                );
                result.put(jsonKey, jsonObjects);
            }
        });
        return result;
    }

    public void deleteByIdWithJsonSchema(String key, JSONObject jsonObject){
        jsonObject.keySet().forEach(jsonKey -> {
            JSONObject childObject = (JSONObject) jsonObject.get(jsonKey);
            String type = (String)childObject.get("type");
            if(type.equals("object")){
                String objectKey = key+":"+jsonKey;
                String childKey = (String)redisTemplate.opsForSet().members(objectKey).iterator().next();
                redisTemplate.delete(objectKey);
                deleteByIdWithJsonSchema(childKey, (JSONObject)childObject.get("properties"));
            }else if(type.equals("array")){
                String objectKey = key+":"+jsonKey;
                JSONObject items = (JSONObject)((JSONArray) childObject.get("items")).get(0);
                Set<Object> keys = redisTemplate.opsForSet().members(objectKey);
                keys.forEach(k ->
                        deleteByIdWithJsonSchema((String) k, (JSONObject) items.get("properties"))
                );
                redisTemplate.delete(objectKey);
            }
        });
        redisTemplate.delete(key);
    }

    private String saveEtag(JSONObject jsonObject, String key){
        String hashString = etagService.generateHash(jsonObject.toJSONString());
        redisTemplate.opsForHash().put(key, "etag", hashString);
        return hashString;
    }

    public String getEtag(String key){
        return (String)redisTemplate.opsForHash().get(key, "etag");
    }

    public boolean isPresent(String key){
        return redisTemplate.hasKey(key);
    }

}

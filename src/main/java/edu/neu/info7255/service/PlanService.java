package edu.neu.info7255.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlanService {
    @Value("#{'${jsonSchema.Integer}'.trim().isEmpty() ? new String[] {} : '${jsonSchema.Integer}'.split(',')}")
    private Set<String> numbers;

    private final EtagService etagService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    public PlanService(EtagService etagService, RedisTemplate<String, Object> redisTemplate){
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

    public JSONObject getById(String key){
        JSONObject jsonObject = new JSONObject();
        Set<String> keys = redisTemplate.keys(key + ":*");
        if(keys == null){
            return jsonObject;
        }
        keys.add(key);
        keys.forEach(jsonKey -> {
            if(jsonKey.equals(key)){
                Map<Object, Object> properties = redisTemplate.opsForHash().entries(jsonKey);
                properties.forEach((k, v) ->{
                    if(k.toString().equals("etag")) return;
                    if(numbers.contains(k.toString())){
                        jsonObject.put(k, Integer.parseInt(v.toString()));
                    }else{
                        jsonObject.put(k, v);
                    }
                });
            }else{
                String curKey = jsonKey.substring(key.length() + 1);
                Set<Object> values = redisTemplate.opsForSet().members(jsonKey);
                if(values != null){
                    if(values.size() > 1 || curKey.equals("linkedPlanServices")){
                        List<Object> curValues = new ArrayList<>();
                        values.forEach(value ->
                            curValues.add(getById((String)value))
                        );
                        jsonObject.put(curKey, curValues);
                    }else{
                        jsonObject.put(curKey, getById((String)values.iterator().next()));
                    }
                }
            }
        });
        return jsonObject;
    }

    public void deleteById(String key){
        Set<String> keys = redisTemplate.keys(key + ":*");
        if(keys == null) return;
        keys.add(key);
        keys.forEach(jsonKey -> {
            if(!jsonKey.equals(key)){
                Set<Object> values = redisTemplate.opsForSet().members(jsonKey);
                if(values == null) return;
                values.forEach(value -> deleteById((String)value));
            }
            redisTemplate.delete(jsonKey);
        });
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

package edu.neu.info7255.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ValidateTest {
    @Autowired
    private JsonSchema jsonSchema;
    @Test
    public void JsonSchemaValidate() throws IOException {
        JsonNode jsonNode = new ObjectMapper()
                .readTree(Validate.class.getResourceAsStream("/a1-use-case.json"));
        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        if(!errors.isEmpty()){
            for(ValidationMessage error : errors){
                System.out.println(error.toString());
            }
        }else{
            System.out.println("Json is valid!");
        }
        assert errors.isEmpty();
    }
}

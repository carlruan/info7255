package edu.neu.info7255.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class Validate {
    @Autowired
    private JsonSchema jsonSchema;
    public Set<ValidationMessage> jsonValidate() throws IOException {
        JsonNode jsonNode = new ObjectMapper()
                .readTree(Validate.class.getResourceAsStream("/a1-use-case.json"));
        return jsonSchema.validate(jsonNode);
    }
}

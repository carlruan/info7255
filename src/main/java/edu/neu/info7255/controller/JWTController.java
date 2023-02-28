package edu.neu.info7255.controller;

import com.nimbusds.jose.JOSEException;
import edu.neu.info7255.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class JWTController {

    private TokenGenerator tokenGenerator;
    @Autowired
    public JWTController(TokenGenerator tokenGenerator){
        this.tokenGenerator = tokenGenerator;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> getToken() throws JOSEException {
        var map = new HashMap<String, String>();
        map.put("token", tokenGenerator.generateToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}

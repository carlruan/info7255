package edu.neu.info7255.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;


@Component
public class TokenGenerator {

    private RSAKey rsaJWK;
    public TokenGenerator(RSAKey rsaJWK){
        this.rsaJWK = rsaJWK;
    }
    @Value("${jwt.audience}")
    private List<String> audience;
    public String generateToken() throws JOSEException {
        JWSSigner signer = new RSASSASigner(rsaJWK);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("kaifeng")
                .issuer("https://kaifengruan.me")
                .audience(audience)
                .issueTime(new Date())
                .expirationTime(new Date(new Date().getTime() + 60 * 60 * 1000))
                .claim("scope", "read write patch delete")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                claimsSet);

        signedJWT.sign(signer);
        String s = signedJWT.serialize();
        return s;
    }

    public boolean validateToken(String s) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(s);
        RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();
        JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
        return signedJWT.verify(verifier);
    }
}

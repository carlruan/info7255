package edu.neu.info7255.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class EtagService {
    @Value("${etag.hashAlgorithm}")
    private String hashAlgorithm;

    public String generateHash(String src){
        MessageDigest md;
        String hashString = "";
        try {
            md = MessageDigest.getInstance(hashAlgorithm);
            byte[] messageDigest = md.digest(src.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            hashString = number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "\"" + hashString + "\"";
    }
}

package edu.neu.info7255.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{

    private HttpStatus httpStatus;

    public CustomException(String message, HttpStatus httpStatus){
        super(message);
        setHttpStatus(httpStatus);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}

package edu.neu.info7255.handler;

import edu.neu.info7255.exception.CustomException;
import edu.neu.info7255.model.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;

@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Message> handleException(CustomException e){
        Error err = new Error(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(new Message(e.getMessage()));
    }
}

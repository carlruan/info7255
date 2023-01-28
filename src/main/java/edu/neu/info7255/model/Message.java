package edu.neu.info7255.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String content;
    private Date timestamp;

    public Message(String content) {
        this.content = content;
        this.timestamp = new Date();
    }
}

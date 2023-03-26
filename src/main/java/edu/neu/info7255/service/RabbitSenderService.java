package edu.neu.info7255.service;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitSenderService {

    private RabbitTemplate rabbitTemplate;
    private Queue queue;

    @Autowired
    public RabbitSenderService(RabbitTemplate rabbitTemplate, Queue queue){
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
    }

    public void send(String order) {
        rabbitTemplate.convertAndSend(this.queue.getName(), order);
    }
}

package com.kafka.SpringBootKafkaDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaSender {

    @Autowired
    private KafkaTemplate<String, String> template;

    String topicName = "dummy";

    public void sendMessage(String message){
//        template.send(topicName, message);
        template.send(topicName, 0, "1", message);
    }
}

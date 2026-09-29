package com.kafka.SpringBootKafkaDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaRestController {

    @Autowired
    private KafkaSender kafkaSender;

    @GetMapping()
    public String producer(@RequestParam("message") String message){
        kafkaSender.sendMessage(message);

        return "Message produced successfully";
    }

}

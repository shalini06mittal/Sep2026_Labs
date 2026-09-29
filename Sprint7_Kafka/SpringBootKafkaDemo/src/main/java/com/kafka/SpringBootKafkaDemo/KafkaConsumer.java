package com.kafka.SpringBootKafkaDemo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "dummy", groupId = "group1")
    public void consumeMessage(String message){
        System.out.println("Message recieved "+message);
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic="dummy",
            partitionOffsets = {
            @PartitionOffset(partition = "0", initialOffset = "0")
            }), groupId = "group2")
    public void consumeMessage1(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
            ){
        System.out.println("Message recieved !!! "+message+ " from partition "+partition);
    }
}

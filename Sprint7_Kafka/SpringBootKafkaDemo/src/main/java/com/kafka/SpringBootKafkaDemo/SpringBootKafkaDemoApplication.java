package com.kafka.SpringBootKafkaDemo;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootKafkaDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootKafkaDemoApplication.class, args);
	}

	@Bean
	public NewTopic newTopic(){
		return new NewTopic("dummy", 2, (short)2);
	}
}

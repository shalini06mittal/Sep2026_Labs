package com.training.SpringBootDemo;

import com.training.SpringBootDemo.component.TokenGenerator;
import com.training.SpringBootDemo.entity.Employee;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class SpringBootDemoApplication {

	public static void main(String[] args) {
		ApplicationContext context =
		SpringApplication.run(SpringBootDemoApplication.class, args);

		//System.out.println(context.getBean(TokenGenerator.class).getBound());
		Employee em = context.getBean(Employee.class);
		System.out.println(em.getName());
	}

}

@Component
class Startup implements CommandLineRunner {
	Startup()                      { System.out.println("1-constructor"); }
	@PostConstruct
    void init()     { System.out.println("2-postConstruct"); }
	public void run(String... a)   { System.out.println("3-runner"); }
}

@Component
class Listener {
	@EventListener(ApplicationReadyEvent.class)
	void ready() { System.out.println("4-ready"); }
}
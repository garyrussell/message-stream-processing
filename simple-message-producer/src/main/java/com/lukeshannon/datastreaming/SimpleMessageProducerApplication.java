package com.lukeshannon.datastreaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SimpleMessageProducerApplication {
	
	private static final Logger log = LoggerFactory.getLogger(SimpleMessageProducerApplication.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	public static void main(String[] args) {
		SpringApplication.run(SimpleMessageProducerApplication.class, args);
	}

	@Bean
	public CommandLineRunner Write() {
		return (args) -> {
			List<String> messages = new ArrayList<String>();
			messages.add("I turned myself into a Pickle....I'm pickle Riiiiiiick!");
			messages.add("Do or do not, there is not try");
			messages.add("Game over man.");
			messages.add("This is Sparta!");
			messages.add("Could you pass the salt?");
			messages.add("By the power of Grey Skull!");
			messages.add("We are going to need a bigger boat");
			messages.add("Shop smart, shop S-mart");
			for (int i = 0; i < 100000; i++) {
				int rnd = new Random().nextInt(messages.size());
				log.info("Sending: " + messages.get(rnd));
				rabbitTemplate.convertAndSend(messages.get(rnd));
				log.info("Sent");
			}

		};
	}
}

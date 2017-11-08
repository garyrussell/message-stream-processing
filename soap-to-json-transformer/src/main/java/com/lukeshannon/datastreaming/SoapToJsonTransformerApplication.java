package com.lukeshannon.datastreaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import io.spring.guides.gs_producing_web_service.Country;

@SpringBootApplication
@EnableBinding(Processor.class)
public class SoapToJsonTransformerApplication {
	
	@Autowired
	private Jaxb2Marshaller marshaller;
	
	private static final Logger log = LoggerFactory.getLogger(SoapToJsonTransformerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SoapToJsonTransformerApplication.class, args);
	}
	

	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public Message processesTsysMessage(String payload) {
		log.info("Received: " + payload);
		Country response = (Country)marshaller.unmarshal(new StringSource(payload));
		log.info("Parsed: " + response);
		return new Message(response.getCapital(),response.getName(),response.getPopulation());
	}
	
	@Bean
	public Jaxb2Marshaller getJaxb2Marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setPackagesToScan("io.spring.guides.gs_producing_web_service");
		return marshaller;
	}
}

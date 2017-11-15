/**
 * 
 */
package com.lukeshannon.datastreaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author lshannon
 *
 */
@EnableBinding(Processor.class)
public class Process {
	
	private List<String> routingKeys;
	
	private ObjectMapper mapper;
	
	private static final Logger log = LoggerFactory.getLogger(Process.class);


	@PostConstruct
	private void init() {
		routingKeys = new ArrayList<String>();
		routingKeys.add("java");
		routingKeys.add("net");
		routingKeys.add("log");
		routingKeys.add("db");
		routingKeys.add("file");
		mapper = new ObjectMapper();
	}

	@StreamListener(Processor.INPUT)
	@Output(Processor.OUTPUT)
	public String process(String message) {
		ProcessedMessage messageObj = new ProcessedMessage(message, getRoutingKey());
		String json = null;
		log.debug("Got a message to process: " + message);
		try {
			json = mapper.writeValueAsString(messageObj);
		} catch (JsonProcessingException e) {
			log.error("Unable to convert: " + messageObj.toString() + " to JSON");
		}
		log.info("Processing Complete. Resulting JSON: " + json);
		return json;
	}

	private String getRoutingKey() {
		int rnd = new Random().nextInt(routingKeys.size());
		return routingKeys.get(rnd);
	}

}

package com.lukeshannon.datastreaming;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ProcessorRepositoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessorRepositoryApplication.class, args);
	}
	
	@RestController
	class HomeController {
		
		@Value("${application.uri}")
		private String uri;
		
		@GetMapping("/")
		public String home() {
			if (uri.contains("localhost")) {
				return "Use " + uri + "/simple-message-processor.jar to get the processor, " + " use " + uri + "/routing-rules.groovy to get the router";
			}
			return "Use https://" + uri + "/simple-message-processor.jar get the processor," + " use " + uri + "/routing-rules.groovy to get the router";
		}
	}
}

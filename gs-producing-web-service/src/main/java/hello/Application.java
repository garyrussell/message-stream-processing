package hello;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringResult;

import io.spring.guides.gs_producing_web_service.Country;

@SpringBootApplication
@EnableRabbit
public class Application {
	
	@Autowired
	private Jaxb2Marshaller marshaller;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	
	@Bean
	public CommandLineRunner demo(final CountryRepository countryRepository) {
		return (args) -> {
			for (Country response: countryRepository.findAll()) {
				StringResult stringResult = new StringResult();
				marshaller.marshal(new JAXBElement<Country>(new QName("uri","local"), Country.class, response), stringResult);
				log.info(stringResult.toString());
				rabbitTemplate.convertAndSend(stringResult.toString());
				log.info("sent");
			}
		};
	}
}

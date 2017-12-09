package com.lukeshannon.datastreaming;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jpa.core.JpaExecutor;
import org.springframework.integration.jpa.inbound.JpaPollingChannelAdapter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@SpringBootApplication
public class JavaConsumerApplication {


	private static final Logger log = LoggerFactory.getLogger(JavaConsumerApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(JavaConsumerApplication.class, args);
	}

	@Autowired
	private MessageRepo repository;


	@Bean
	@Transactional
	public CommandLineRunner newRecords(MessageRepo repository) {
		return (args) -> {

			// clean up old
			repository.deleteAll();

			// save a couple of customers
			repository.save(new Message("Key", "Message", false, new Date(), Long.valueOf(1)));
			repository.save(new Message("Key2", "Message2", false, new Date(), Long.valueOf(2)));


			// For testing
			// fetch all the customers
			log.info("(Before Processing) Messages found with findAll():");
			log.info("-------------------------------");
			for (Message message : repository.findAll()) {
				log.info(message.toString());
			}
			log.info("");

		};
	}

	@Bean
	public PollerMetadata poller(PlatformTransactionManager tm) {
		PollerMetadata poller = new PollerMetadata();
		poller.setTrigger(new PeriodicTrigger(5, TimeUnit.SECONDS));
		poller.setAdviceChain(Arrays.asList(consumerTransactionInterceptor(tm)));
		poller.setMaxMessagesPerPoll(1); // 1 collection per poll
		return poller;
	}

	@Bean
	public TransactionInterceptor consumerTransactionInterceptor(PlatformTransactionManager tm) {
		TransactionInterceptor inter = new TransactionInterceptor();
		inter.setTransactionManager(tm);
		inter.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());
		return inter;
	}

	@Bean
	public JpaExecutor executor(EntityManagerFactory entityManagerFactory) {
		JpaExecutor executor = new JpaExecutor(entityManagerFactory);
		executor.setJpaQuery("from Message where processed = false");
		return executor;
	}

	@InboundChannelAdapter(channel = "fromdb", poller = @Poller("poller"))
	@Bean
	public MessageSource<?> source(EntityManagerFactory entityManagerFactory) {
		return new JpaPollingChannelAdapter(executor(entityManagerFactory));
	}

	private boolean doneAbort;

	private boolean addAnotherDone;

	@ServiceActivator(inputChannel = "fromdb")
	public void foo(List<Message> in) {
		for (Message m : in) {
			m.setProcessed(true);
		}
		log.info("Processed: " +  in.toString());
		// For testing
					// fetch all the customers
					log.info("(After Processing) Messages found with findAll():");
					log.info("-------------------------------");
					for (Message message : repository.findAll()) {
						log.info(message.toString());
					}
					log.info("");
		this.repository.save(in);
		if (!doneAbort) {
			log.info("Rolling back");
			doneAbort = true;
			throw new RuntimeException("foo");
		}
		if (!addAnotherDone) {
			addAnotherDone = true;
			repository.save(new Message("Key3", "Message3", false, new Date(), Long.valueOf(3)));
			log.info("Added another");
		}
 	}

}

package com.lukeshannon.datastreaming;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
		poller.setAdviceChain(Arrays.asList(transactionInterceptor(tm)));
		return poller;
	}
	
	@Bean
	public TransactionInterceptor transactionInterceptor(PlatformTransactionManager tm) {
		TransactionInterceptor inter = new TransactionInterceptor();
		inter.setTransactionManager(tm);
		inter.setTransactionAttributeSource(null);
		return inter;
	}
	
	/*
	 * TODO: Make this transactions so this will not update the record till the ServiceActivator has got it
	 * @param dataSource
	 * @return
	 */
//	@InboundChannelAdapter(channel = "fromdb", poller = @Poller(fixedDelay = "5000", maxMessagesPerPoll="-1"))
//	@InboundChannelAdapter(channel = "fromdb", poller = @Poller("poller"))
//	@Bean
//	public MessageSource<?> source(DataSource dataSource) {
//		JdbcPollingChannelAdapter adapter = new JdbcPollingChannelAdapter(dataSource, "SELECT * FROM message WHERE processed = false");
//		adapter.setUpdateSql("UPDATE message SET processed = true WHERE id in (:id)");
//		return adapter;
//	}

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

	boolean done;
	
	@ServiceActivator(inputChannel = "fromdb")
	public void foo(List<Message> in) {

		log.info("Processed: " +  in.toString());
		// For testing
					// fetch all the customers
					log.info("(After Processing) Messages found with findAll():");
					log.info("-------------------------------");
					for (Message message : repository.findAll()) {
						log.info(message.toString());
					}
					log.info("");
		for (Message m : in) {
			m.setProcessed(true);
			this.repository.save(m);
		}
		if (!done) {
			done = true;
			throw new RuntimeException("foo");
		}
 	}

}

/**
 * 
 */
package hello;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lshannon
 *
 */
@Configuration
public class MessageQueueConfig {
	
	@Value("${rabbit_messages}")
	public String rabbit_messages;
	
	@Bean
	public FanoutExchange messageExchange() {
		return new FanoutExchange(rabbit_messages, true, false);
	}
	
	@Bean 
	public Binding binding() {
		return new Binding(rabbit_messages, Binding.DestinationType.QUEUE, rabbit_messages, new String(), null);
	}
	
	@Bean
	// Every queue is bound to the default direct exchange
	public Queue messageQueue() {
		return new Queue(rabbit_messages);
	}

	
	  @Bean
	    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
	        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
	        rabbitTemplate.setExchange(rabbit_messages);
	        return rabbitTemplate;
	    }

}

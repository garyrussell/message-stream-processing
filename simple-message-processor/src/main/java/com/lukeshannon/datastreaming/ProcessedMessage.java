/**
 * 
 */
package com.lukeshannon.datastreaming;

/**
 * @author lshannon
 *
 */
public class ProcessedMessage {
	
	private String message;
	private String routingKey;
	
	public ProcessedMessage() {
	}
	
	public ProcessedMessage(String message, String routingKey) {
		this.message = message;
		this.routingKey = routingKey;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	@Override
	public String toString() {
		return "ProcessedMessage [message=" + message + ", routingKey=" + routingKey + "]";
	}

}

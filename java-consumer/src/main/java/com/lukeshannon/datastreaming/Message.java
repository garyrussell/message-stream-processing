package com.lukeshannon.datastreaming;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="message")
public class Message {
	
	private String routingKey;
	private String message;
	private boolean processed;
	private Date processedDate;
	@Id
	private Long id;
	
	public Message() {
		
	}
	
	
	public Message(String routingKey, String message, boolean processed, Date processedDate, Long id) {
		this.routingKey = routingKey;
		this.message = message;
		this.processed = processed;
		this.processedDate = processedDate;
		this.id = id;
	}


	public String getRoutingKey() {
		return routingKey;
	}
	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isProcessed() {
		return processed;
	}
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	public Date getProcessedDate() {
		return processedDate;
	}
	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "Message [routingKey=" + routingKey + ", message=" + message + ", processed=" + processed
				+ ", processedDate=" + processedDate + ", id=" + id + "]";
	}
	
	
	


}

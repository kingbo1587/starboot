package com.wh.starboot.model;

import java.util.UUID;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wh.starboot.exception.InvalidQueueMessageException;

public class QueueMessage {

	private String taskId;
	private String payload;
	private int type;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public QueueMessage() {
		taskId = UUID.randomUUID().toString();
	}

	public QueueMessage(String payload) {
		taskId = UUID.randomUUID().toString();
		this.payload = payload;
	}

	public static QueueMessage fromAmqpMessage(Message message) {
		ObjectMapper objectMapper = new ObjectMapper();
		QueueMessage msg = null;
		try {
			msg = objectMapper.readValue(new String(message.getBody(), "utf-8"), QueueMessage.class);
		} catch (Exception e) {
			throw new InvalidQueueMessageException();
		}
		return msg;
	}

	public Message toAmqpMessage() {
		ObjectMapper objectMapper = new ObjectMapper();
		Message message = null;
		try {
			message = MessageBuilder.withBody(objectMapper.writeValueAsString(this).getBytes("UTF-8")).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidQueueMessageException();
		}
		return message;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}

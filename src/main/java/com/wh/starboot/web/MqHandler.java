package com.wh.starboot.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.wh.starboot.model.QueueMessage;

@Component
public class MqHandler {

	private static final Logger logger = LoggerFactory.getLogger(MqHandler.class);

	@RabbitListener(queues = "${mq.queue.test}")
	public void handleMessage(Message message) {
		logger.info("handleMessage|Enter method.");
		try {
			QueueMessage msg = QueueMessage.fromAmqpMessage(message);
			String payload = msg.getPayload();
			logger.info("handleMessage|payload:{}", payload);
		} catch (Exception e) {
			logger.error("handleMessage|exception|", e);
		}
	}
}

package io.cornerstone.core.message.rabbit;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import io.cornerstone.core.message.Queue;
import lombok.Getter;
import lombok.Setter;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

public abstract class RabbitQueue<T extends Serializable> implements Queue<T> {

	@Autowired
	protected RabbitTemplate rabbitTemplate;

	@Autowired
	protected AmqpAdmin amqpAdmin;

	@Getter
	@Setter
	protected String queueName = "";

	@Getter
	@Setter
	protected boolean durable = true;

	public RabbitQueue() {
		Class<?> clazz = ResolvableType.forClass(getClass()).as(RabbitQueue.class).resolveGeneric(0);
		if (clazz == null) {
			throw new IllegalArgumentException(getClass().getName() + " should be generic");
		}
		this.queueName = clazz.getName();
	}

	@PostConstruct
	public void init() {
		this.amqpAdmin
			.declareQueue(new org.springframework.amqp.core.Queue(this.queueName, this.durable, false, false));
	}

	@Override
	public void produce(T message) {
		this.rabbitTemplate.convertAndSend(getQueueName(), message);
	}

}

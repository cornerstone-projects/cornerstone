package io.cornerstone.core.message.rabbit;

import java.io.Serializable;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ResolvableType;

import io.cornerstone.core.Application;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.message.Topic;
import lombok.Getter;
import lombok.Setter;

public abstract class RabbitTopic<T extends Serializable> implements Topic<T> {

	@Autowired
	protected RabbitTemplate rabbitTemplate;

	@Autowired
	protected AmqpAdmin amqpAdmin;

	@Value("${spring.rabbitmq.exchange:default}")
	protected String exchange;

	protected String routingKey = "";

	@Getter
	protected String queueName;

	@Setter
	@Autowired(required = false)
	private Executor taskExecutor;

	public RabbitTopic() {
		Class<?> clazz = ResolvableType.forClass(getClass()).as(RabbitTopic.class).resolveGeneric(0);
		if (clazz == null)
			throw new IllegalArgumentException(getClass().getName() + " should be generic");
		routingKey = clazz.getName();
	}

	@PostConstruct
	public void init() {
		amqpAdmin.declareExchange(new TopicExchange(exchange, true, false));
		Queue queue = amqpAdmin.declareQueue();
		if (queue != null) {
			queueName = queue.getName();
			amqpAdmin.declareBinding(
					new Binding(queueName, DestinationType.QUEUE, exchange, getRoutingKey(Scope.GLOBAL), null));
			amqpAdmin.declareBinding(
					new Binding(queueName, DestinationType.QUEUE, exchange, getRoutingKey(Scope.APPLICATION), null));
		}
	}

	@PreDestroy
	public void destroy() {
		amqpAdmin.deleteQueue(queueName);
	}

	protected String getRoutingKey(Scope scope) {
		if (scope == null || scope == Scope.LOCAL)
			return null;
		StringBuilder sb = new StringBuilder(routingKey).append(".");
		if (scope == Scope.APPLICATION)
			Application.current().ifPresent(a -> sb.append(a.getName()));
		return sb.toString();
	}

	@Override
	public void publish(final T message, Scope scope) {
		if (scope == null)
			scope = Scope.GLOBAL;
		if (scope == Scope.LOCAL) {
			Runnable task = () -> subscribe(message);
			if (taskExecutor != null)
				taskExecutor.execute(task);
			else
				task.run();
		} else {
			rabbitTemplate.convertAndSend(exchange, getRoutingKey(scope), message);
		}
	}
}

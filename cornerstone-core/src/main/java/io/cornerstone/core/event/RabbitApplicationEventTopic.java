package io.cornerstone.core.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.cornerstone.core.message.rabbit.RabbitTopic;

@ConditionalOnProperty(name = "application-event.topic.type", havingValue = "rabbit")
@Primary
@Component
public class RabbitApplicationEventTopic extends RabbitTopic<ApplicationEvent> implements ApplicationEventTopic {

	@Autowired
	private ApplicationContext ctx;

	@RabbitListener(queues = "#{@rabbitApplicationEventTopic.queueName}")
	@Override
	public void subscribe(ApplicationEvent event) {
		log.info("Receive published message: {}", event);
		this.ctx.publishEvent(event);
	}

}

package io.cornerstone.core.event;

import io.cornerstone.core.message.redis.RedisTopic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnProperty(name = "application-event.topic.type", havingValue = "redis", matchIfMissing = true)
@Component
public class RedisApplicationEventTopic extends RedisTopic<ApplicationEvent> implements ApplicationEventTopic {

	@Autowired
	private ApplicationContext ctx;

	@Override
	public void subscribe(ApplicationEvent event) {
		this.ctx.publishEvent(event);
	}

}

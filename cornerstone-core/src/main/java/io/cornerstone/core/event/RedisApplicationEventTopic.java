package io.cornerstone.core.event;

import io.cornerstone.core.message.redis.RedisTopic;
import io.cornerstone.core.redis.RedisEnabled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

@RedisEnabled
@Component
public class RedisApplicationEventTopic extends RedisTopic<ApplicationEvent> implements ApplicationEventTopic {

	@Autowired
	private ApplicationContext ctx;

	@Override
	public void subscribe(ApplicationEvent event) {
		this.ctx.publishEvent(event);
	}

}

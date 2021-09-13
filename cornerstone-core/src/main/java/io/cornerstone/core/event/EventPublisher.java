package io.cornerstone.core.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.cornerstone.core.domain.Scope;

@Component
public class EventPublisher {

	@Autowired
	private ApplicationContext ctx;

	@Autowired(required = false)
	private ApplicationEventTopic applicationEventTopic;

	public void publish(ApplicationEvent event, final Scope scope) {
		if ((this.applicationEventTopic != null) && (scope != null) && (scope != Scope.LOCAL))
			this.applicationEventTopic.publish(event, scope);
		else
			this.ctx.publishEvent(event);
	}

	@EventListener
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event.getApplicationContext() != this.ctx)
			return;
		if (event instanceof ContextRefreshedEvent) {
			publish(new InstanceStartupEvent(), Scope.GLOBAL);
		} else if (event instanceof ContextClosedEvent) {
			publish(new InstanceShutdownEvent(), Scope.GLOBAL);
		}
	}

}

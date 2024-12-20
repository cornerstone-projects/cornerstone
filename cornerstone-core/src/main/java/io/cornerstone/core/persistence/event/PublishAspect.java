package io.cornerstone.core.persistence.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.cornerstone.core.aop.BaseAspect;
import io.cornerstone.core.event.EventPublisher;
import io.cornerstone.core.util.ReflectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static io.cornerstone.core.persistence.event.EntityOperationType.*;

@Aspect
@Component
public class PublishAspect extends BaseAspect implements TransactionSynchronization {

	private static final String HIBERNATE_EVENTS = "HIBERNATE_EVENTS_FOR_PUBLISH";

	@Autowired
	private EventPublisher eventPublisher;

	public PublishAspect() {
		this.order = 1;
	}

	@Pointcut("execution(public void jakarta.persistence.EntityManager.persist(..))")
	private void persist() {
	}

	@Pointcut("execution(public * jakarta.persistence.EntityManager..merge(..))")
	private void merge() {
	}

	@Pointcut("execution(public void jakarta.persistence.EntityManager..remove(..))")
	private void remove() {
	}

	@Before("persist() || merge() || remove()")
	public void registerTransactionSynchronization(JoinPoint jp) {
		if (!isBypass() && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
				&& !TransactionSynchronizationManager.getSynchronizations().contains(this)) {
			TransactionSynchronizationManager.registerSynchronization(this);
		}
	}

	@Override
	public void afterCommit() {
		List<AbstractEvent> events = getHibernateEvents(false);
		if ((events == null) || events.isEmpty()) {
			return;
		}
		Map<Persistable<?>, EntityOperationType> actions = new HashMap<>();
		for (AbstractEvent event : events) {
			Object entity;
			EntityOperationType action;
			switch (event) {
				case PostInsertEvent evt -> {
					entity = evt.getEntity();
					action = CREATE;
				}
				case PostUpdateEvent evt -> {
					entity = evt.getEntity();
					action = UPDATE;
				}
				case PostDeleteEvent evt -> {
					entity = evt.getEntity();
					action = DELETE;
				}
				case null, default -> {
					continue;
				}
			}
			EntityOperationType previousAction = actions.get(entity);
			if ((action == UPDATE) && (previousAction == CREATE)) {
				action = CREATE;
			}
			actions.put((Persistable<?>) entity, action);
		}
		actions.forEach((k, v) -> {
			PublishAware publishAware = ReflectionUtils.getEntityClass(k).getAnnotation(PublishAware.class);
			if (publishAware != null) {
				this.eventPublisher.publish(new EntityOperationEvent<>(k, v), publishAware.scope());
			}
		});
	}

	@Override
	public void afterCompletion(int status) {
		if (TransactionSynchronizationManager.hasResource(HIBERNATE_EVENTS)) {
			TransactionSynchronizationManager.unbindResource(HIBERNATE_EVENTS);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<AbstractEvent> getHibernateEvents(boolean create) {
		if (create && !TransactionSynchronizationManager.hasResource(HIBERNATE_EVENTS)) {
			TransactionSynchronizationManager.bindResource(HIBERNATE_EVENTS, new ArrayList<>());
		}
		return (List<AbstractEvent>) TransactionSynchronizationManager.getResource(HIBERNATE_EVENTS);
	}

}

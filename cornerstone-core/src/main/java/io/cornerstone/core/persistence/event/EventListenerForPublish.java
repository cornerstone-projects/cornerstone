package io.cornerstone.core.persistence.event;

import io.cornerstone.core.util.ReflectionUtils;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventListenerForPublish
		implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@PostConstruct
	private void init() {
		SessionFactoryImpl sessionFactory = this.entityManagerFactory.unwrap(SessionFactoryImpl.class);
		EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
		registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(this);
		registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(this);
		registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(this);
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if (isAnnotated(event.getEntity())) {
			PublishAspect.getHibernateEvents(true).add(event);
		}
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		if (isAnnotated(event.getEntity())) {
			PublishAspect.getHibernateEvents(true).add(event);
		}
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (isAnnotated(event.getEntity())) {
			PublishAspect.getHibernateEvents(true).add(event);
		}
	}

	@Override
	public void onPostDeleteCommitFailed(PostDeleteEvent event) {

	}

	@Override
	public void onPostUpdateCommitFailed(PostUpdateEvent event) {

	}

	@Override
	public void onPostInsertCommitFailed(PostInsertEvent event) {

	}

	@Override
	public boolean requiresPostCommitHandling(EntityPersister persister) {
		return false;
	}

	private static boolean isAnnotated(Object entity) {
		return ReflectionUtils.getEntityClass(entity).isAnnotationPresent(PublishAware.class);
	}

}

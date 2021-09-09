package io.cornerstone.core.hibernate.event;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

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

import io.cornerstone.core.util.ReflectionUtils;

@Component
public class EventListenerForPublish
		implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

	private static final long serialVersionUID = 9062685218966998574L;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@PostConstruct
	private void init() {
		SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
		EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
		registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(this);
		registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(this);
		registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(this);
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if (isAnnotated(event.getEntity()))
			PublishAspect.getHibernateEvents(true).add(event);
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		if (isAnnotated(event.getEntity()))
			PublishAspect.getHibernateEvents(true).add(event);
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (isAnnotated(event.getEntity()))
			PublishAspect.getHibernateEvents(true).add(event);
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
	@Deprecated
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}

	private static boolean isAnnotated(Object entity) {
		return ReflectionUtils.getEntityClass(entity).isAnnotationPresent(PublishAware.class);
	}
}
package io.cornerstone.core.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.ProxyUtils;
import org.springframework.transaction.annotation.Transactional;

import io.cornerstone.core.hibernate.domain.AbstractTreeableEntity;

@Transactional
public class TreeableRepositoryImpl<T extends AbstractTreeableEntity<T>> implements TreeableRepository<T> {

	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public <S extends T> S save(S entity) {
		T parent = entity.getParent();
		if (parent != null) {
			Class<T> entityClass = (Class<T>) ProxyUtils.getUserClass(parent);
			if (parent.getClass() != entityClass // uninitialized proxy
					|| parent.getId() != null && parent.getFullId() == null) {
				parent = (T) entityManager.find(entityClass, parent.getId());
				entity.setParent(parent);
			}
		}
		if (entity.isNew()) {
			entity.setLevel(parent != null ? parent.getLevel() + 1 : 1);
			entity.setFullId(UUID.randomUUID().toString());
			entityManager.persist(entity);
			entityManager.flush();
			String fullId = String.valueOf(entity.getId()) + ".";
			if (parent != null)
				fullId = parent.getFullId() + fullId;
			entity.setFullId(fullId);
			return entity;
		} else {
			boolean positionChanged = (parent == null && entity.getLevel() != 1
					|| parent != null && (entity.getLevel() - parent.getLevel() != 1
							|| !entity.getFullId().startsWith(parent.getFullId())));
			if (positionChanged) {
				String fullId = String.valueOf(entity.getId()) + ".";
				if (parent != null)
					fullId = parent.getFullId() + fullId;
				entity.setFullId(fullId);
				entity.setLevel(fullId.split("\\.").length);
				if (parent != null)
					entity.setParent(parent); // recalculate fullname
			}
			S merged = entityManager.merge(entity);
			if (positionChanged) {
				Collection<T> children = merged.getChildren();
				if (children != null) {
					for (T c : children)
						save(c);
				}
			}
			return merged;
		}
	}

	@Override
	public <S extends T> S saveAndFlush(S entity) {
		S result = save(entity);
		entityManager.flush();
		return result;
	}

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {
		List<S> result = new ArrayList<S>();
		for (S entity : entities) {
			result.add(save(entity));
		}
		return result;
	}

	@Override
	public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
		List<S> result = saveAll(entities);
		entityManager.flush();
		return result;
	}
}

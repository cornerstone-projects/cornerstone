package io.cornerstone.core.web;

import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import io.cornerstone.core.hibernate.criteria.PredicateBuilder;
import io.cornerstone.core.hibernate.domain.AbstractTreeableEntity;

public abstract class AbstractTreeableEntityController<T extends AbstractTreeableEntity<T>>
		extends EntityControllerBase<T, Long> {

	public List<T> children(Long id, String query, T example) {
		Specification<T> spec = (root, cq, cb) -> {
			Predicate predicate = (id == null || id < 1) ? cb.isNull(root.get("parent"))
					: cb.equal(root.get("parent").get("id"), id);
			if (StringUtils.hasText(query)) {
				predicate = cb.and(predicate, getQuerySpecification(query).toPredicate(root, cq, cb));
			} else {
				predicate = PredicateBuilder.andExample(root, cb, predicate, Example.of(example, getExampleMatcher()));
			}
			return predicate;
		};
		return specificationExecutor.findAll(spec);
	}

	public List<T> list(String query, T example) {
		if (StringUtils.hasText(query)) {
			return specificationExecutor.findAll(getQuerySpecification(query));
		} else {
			return repository.findAll(Example.of(example, getExampleMatcher()));
		}
	}

	protected void beforeSave(T entity) {
		validateName(entity, entity.getId());
	}

	protected void beforeUpdate(Long id, T entity) {
		validateName(entity, id);
	}

	private void validateName(T entity, @Nullable Long id) {
		T parent = entity.getParent();
		Specification<T> spec = (root, cq, cb) -> {
			Predicate p = cb.and(parent != null ? cb.equal(root.get("parent"), parent) : cb.isNull(root.get("parent")),
					cb.equal(root.get("name"), entity.getName()));
			return id != null ? cb.and(p, cb.notEqual(root.get("id"), id)) : p;
		};
		if (specificationExecutor.count(spec) > 0)
			throw badRequest("name.already.exists");
	}

}

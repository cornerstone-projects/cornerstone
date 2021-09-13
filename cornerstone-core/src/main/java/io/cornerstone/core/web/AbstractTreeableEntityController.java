package io.cornerstone.core.web;

import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonView;

import io.cornerstone.core.domain.View;
import io.cornerstone.core.hibernate.criteria.PredicateBuilder;
import io.cornerstone.core.hibernate.domain.AbstractTreeableEntity;
import springfox.documentation.annotations.ApiIgnore;

public abstract class AbstractTreeableEntityController<T extends AbstractTreeableEntity<T>>
		extends AbstractEntityController<T, Long> {

	@GetMapping(PATH_DETAIL + "/children")
	@JsonView(View.List.class)
	public List<T> children(@PathVariable Long id, @RequestParam(required = false) String query, @ApiIgnore T example) {
		Specification<T> spec = (root, cq, cb) -> {
			Predicate predicate = ((id == null) || (id < 1)) ? cb.isNull(root.get("parent"))
					: cb.equal(root.get("parent").get("id"), id);
			if (StringUtils.hasText(query)) {
				predicate = cb.and(predicate, getQuerySpecification(query).toPredicate(root, cq, cb));
			} else {
				predicate = PredicateBuilder.andExample(root, cb, predicate, Example.of(example, getExampleMatcher()));
			}
			return predicate;
		};
		return this.specificationExecutor.findAll(spec);
	}

	@Override
	protected void beforeSave(T entity) {
		validateName(entity, entity.getId());
	}

	@Override
	protected void beforeUpdate(Long id, T entity) {
		validateName(entity, id);
	}

	@Override
	protected void beforePatch(Long id, T entity) {
		if (entity.getName() != null) // name present
			validateName(entity, id);
	}

	private void validateName(T entity, @Nullable Long id) {
		T parent = entity.getParent();
		Specification<T> spec = (root, cq, cb) -> {
			Predicate p = cb.and(parent != null ? cb.equal(root.get("parent"), parent) : cb.isNull(root.get("parent")),
					cb.equal(root.get("name"), entity.getName()));
			return id != null ? cb.and(p, cb.notEqual(root.get("id"), id)) : p;
		};
		if (this.specificationExecutor.count(spec) > 0)
			throw badRequest("name.already.exists");
	}

}

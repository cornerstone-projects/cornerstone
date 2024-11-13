package io.cornerstone.core.web;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.persistence.criteria.PredicateBuilder;
import io.cornerstone.core.persistence.domain.AbstractTreeableEntity;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static io.cornerstone.core.persistence.domain.AbstractTreeableEntity_.*;

public abstract class AbstractTreeableEntityController<T extends AbstractTreeableEntity<T>>
		extends AbstractEntityController<T, Long> {

	@PostConstruct
	private void init() {
		if (this.specificationExecutor == null) {
			Class<?> repositoryClass = ClassUtils.getAllInterfaces(this.repository)[0];
			throw new RuntimeException(repositoryClass + " should extends " + JpaSpecificationExecutor.class.getName()
					+ "<" + this.entityClass.getName() + ">");
		}
	}

	@GetMapping(PATH_DETAIL + "/children")
	@JsonView(View.List.class)
	public List<T> children(@PathVariable Long id, @RequestParam(required = false) String query,
			@Parameter(hidden = true) T example) {
		Specification<T> spec = (root, cq, cb) -> {
			Predicate predicate = ((id == null) || (id < 1)) ? cb.isNull(root.get(PARENT))
					: cb.equal(root.get(PARENT).get(ID), id);
			if (StringUtils.hasText(query)) {
				predicate = cb.and(predicate, getQuerySpecification(query).toPredicate(root, cq, cb));
			}
			else {
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
		if (entity.getName() != null) {
			validateName(entity, id);
		}
	}

	private void validateName(T entity, @Nullable Long id) {
		T parent = entity.getParent();
		Specification<T> spec = (root, cq, cb) -> {
			Predicate p = cb.and(parent != null ? cb.equal(root.get(PARENT), parent) : cb.isNull(root.get(PARENT)),
					cb.equal(root.get(NAME), entity.getName()));
			return id != null ? cb.and(p, cb.notEqual(root.get(ID), id)) : p;
		};
		if (this.specificationExecutor.count(spec) > 0) {
			throw badRequest("name.already.exists");
		}
	}

}

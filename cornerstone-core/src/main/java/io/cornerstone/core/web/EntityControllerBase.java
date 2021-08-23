package io.cornerstone.core.web;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.core.ResolvableType;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.ClassUtils;

import io.cornerstone.core.domain.Versioned;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.util.BeanUtils;

abstract class EntityControllerBase<T extends Persistable<ID>, ID> extends BaseRestController {

	protected final Class<T> entityClass;

	protected JpaRepository<T, ID> repository;

	protected JpaSpecificationExecutor<T> specificationExecutor;

	@SuppressWarnings("unchecked")
	protected EntityControllerBase() {
		entityClass = (Class<T>) ResolvableType.forClass(getClass()).as(EntityControllerBase.class).getGeneric(0)
				.resolve();
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		repository = (JpaRepository<T, ID>) applicationContext.getBeanProvider(
				ResolvableType.forClassWithGenerics(JpaRepository.class, entityClass, Long.class), false).getObject();
		specificationExecutor = (JpaSpecificationExecutor<T>) applicationContext.getBeanProvider(
				ResolvableType.forClassWithGenerics(JpaSpecificationExecutor.class, entityClass), false).getObject();
	}

	public T save(@Valid T entity) {
		beforeSave(entity);
		return repository.save(entity);
	}

	public T get(ID id) {
		return repository.findById(id).orElseThrow(() -> notFound(id));
	}

	public void update(ID id, @Valid T entity) {
		beforeUpdate(id, entity);
		repository.findById(id).map(en -> {
			BeanUtils.copyPropertiesInJsonView(entity, en, determineViewForUpdate(entity));
			return repository.save(en);
		}).orElseThrow(() -> notFound(id));
	}

	public T patch(ID id, @Valid T entity) {
		beforeUpdate(id, entity);
		return repository.findById(id).map(en -> {
			BeanUtils.copyNonNullProperties(entity, en);
			return repository.save(en);
		}).orElseThrow(() -> notFound(id));
	}

	public void delete(ID id) {
		T entity = repository.findById(id).orElseThrow(() -> notFound(id));
		beforeDelete(entity);
		repository.delete(entity);
	}

	protected Specification<T> getQuerySpecification(String query) {
		String q = '%' + query + '%';
		return (root, cq, cb) -> cb.like(root.get("name"), q);
	}

	protected ExampleMatcher getExampleMatcher() {
		return ExampleMatcher.matching().withMatcher("name", match -> match.contains());
	}

	protected void beforeSave(T entity) {

	}

	protected void beforeUpdate(ID id, T entity) {

	}

	protected void beforeDelete(T entity) {

	}

	protected Class<?> determineViewForUpdate(T entity) {
		return findViewForEntity(
				(entity instanceof Versioned && ((Versioned) entity).getVersion() == null) ? View.Edit.class
						: View.Update.class,
				entityClass);
	}

	private static Class<?> findViewForEntity(Class<?> defaultView, Class<?> entityClass) {
		String entityViewName = entityClass.getName() + "$View$" + defaultView.getSimpleName();
		if (ClassUtils.isPresent(entityViewName, entityClass.getClassLoader())) {
			try {
				return ClassUtils.forName(entityViewName, entityClass.getClassLoader());
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return defaultView;
	}

}

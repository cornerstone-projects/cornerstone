package io.cornerstone.core.web;

import static org.springframework.data.domain.Sort.Direction.DESC;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonView;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.domain.Versioned;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.util.BeanUtils;
import springfox.documentation.annotations.ApiIgnore;

public abstract class AbstractEntityController<T, ID> extends BaseRestController {

	protected static final String PATH_LIST = "/#{T(io.cornerstone.core.util.StringHelper).pluralOf(entityName)}";

	protected static final String PATH_DETAIL = "/#{entityName}/{id:\\d+}";

	protected final Class<T> entityClass;

	protected final Class<ID> idClass;

	protected JpaRepository<T, ID> repository;

	protected JpaSpecificationExecutor<T> specificationExecutor;

	@SuppressWarnings("unchecked")
	protected AbstractEntityController() {
		ResolvableType rt = ResolvableType.forClass(getClass()).as(AbstractEntityController.class);
		entityClass = (Class<T>) rt.getGeneric(0).resolve();
		idClass = (Class<ID>) rt.getGeneric(1).resolve();
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() {
		repository = (JpaRepository<T, ID>) applicationContext
				.getBeanProvider(ResolvableType.forClassWithGenerics(JpaRepository.class, entityClass, idClass), false)
				.getObject();
		specificationExecutor = (JpaSpecificationExecutor<T>) applicationContext.getBeanProvider(
				ResolvableType.forClassWithGenerics(JpaSpecificationExecutor.class, entityClass), false).getObject();
	}

	public String getEntityName() {
		return StringUtils.uncapitalize(entityClass.getSimpleName());
	}

	@JsonView({ View.List.class })
	@GetMapping(PATH_LIST)
	public ResultPage<T> list(@PageableDefault(sort = "id", direction = DESC) Pageable pageable,
			@RequestParam(required = false) String query, @ApiIgnore T example) {
		Page<T> page;
		if (specificationExecutor != null && StringUtils.hasText(query)) {
			page = specificationExecutor.findAll(getQuerySpecification(query), pageable);
		} else {
			page = repository.findAll(Example.of(example, getExampleMatcher()), pageable);
		}
		return ResultPage.of(page);
	}

	@PostMapping(PATH_LIST)
	public T save(@RequestBody @JsonView(View.Creation.class) @Valid T entity) {
		beforeSave(entity);
		return repository.save(entity);
	}

	@GetMapping(PATH_DETAIL)
	public T get(@PathVariable ID id) {
		return repository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable ID id, @RequestBody @JsonView(View.Update.class) @Valid T entity) {
		beforeUpdate(id, entity);
		repository.findById(id).map(en -> {
			BeanUtils.copyPropertiesInJsonView(entity, en, determineViewForUpdate(entity));
			return repository.save(en);
		}).orElseThrow(() -> notFound(id));
	}

	@PatchMapping(PATH_DETAIL)
	public T patch(@PathVariable ID id, @RequestBody @JsonView(View.Update.class) @Valid T entity) {
		beforePatch(id, entity);
		return repository.findById(id).map(en -> {
			BeanUtils.copyNonNullProperties(entity, en);
			return repository.save(en);
		}).orElseThrow(() -> notFound(id));
	}

	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable ID id) {
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

	protected void beforePatch(ID id, T entity) {

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

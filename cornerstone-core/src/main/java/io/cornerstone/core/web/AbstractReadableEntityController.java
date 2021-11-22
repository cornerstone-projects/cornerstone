package io.cornerstone.core.web;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.domain.View;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.data.domain.Sort.Direction.DESC;

public abstract class AbstractReadableEntityController<T, ID> extends BaseRestController {

	protected static final String PATH_LIST = "/#{T(io.cornerstone.core.util.StringHelper).pluralOf(entityName)}";

	protected static final String PATH_DETAIL = "/#{entityName}/{id:\\d+}";

	protected final Class<T> entityClass;

	protected final Class<ID> idClass;

	protected JpaRepository<T, ID> repository;

	protected JpaSpecificationExecutor<T> specificationExecutor;

	@SuppressWarnings("unchecked")
	protected AbstractReadableEntityController() {
		ResolvableType rt = ResolvableType.forClass(getClass()).as(AbstractReadableEntityController.class);
		this.entityClass = (Class<T>) rt.getGeneric(0).resolve();
		this.idClass = (Class<ID>) rt.getGeneric(1).resolve();
	}

	@PostConstruct
	private void init() {
		ObjectProvider<JpaRepository<T, ID>> repositoryProvider = this.applicationContext.getBeanProvider(
				ResolvableType.forClassWithGenerics(JpaRepository.class, this.entityClass, this.idClass), false);
		this.repository = repositoryProvider.getObject();
		ObjectProvider<JpaSpecificationExecutor<T>> specificationExecutorProvider = this.applicationContext
				.getBeanProvider(ResolvableType.forClassWithGenerics(JpaSpecificationExecutor.class, this.entityClass),
						false);
		this.specificationExecutor = specificationExecutorProvider.getIfAvailable(); // optional
	}

	public String getEntityName() {
		return StringUtils.uncapitalize(this.entityClass.getSimpleName());
	}

	@JsonView(View.List.class)
	@GetMapping(PATH_LIST)
	@PageableAsQueryParam
	public ResultPage<T> list(@PageableDefault(sort = "id", direction = DESC) Pageable pageable,
			@RequestParam(required = false) String query, @Parameter(hidden = true) T example) {
		Page<T> page;
		if ((this.specificationExecutor != null) && StringUtils.hasText(query)) {
			page = this.specificationExecutor.findAll(getQuerySpecification(query), pageable);
		}
		else {
			page = this.repository.findAll(Example.of(example, getExampleMatcher()), pageable);
		}
		return ResultPage.of(page);
	}

	@GetMapping(PATH_DETAIL)
	public T get(@PathVariable ID id) {
		return this.repository.findById(id).orElseThrow(() -> notFound(id));
	}

	protected Specification<T> getQuerySpecification(String query) {
		String q = '%' + query + '%';
		return (root, cq, cb) -> cb.like(root.get("name"), q);
	}

	protected ExampleMatcher getExampleMatcher() {
		return ExampleMatcher.matching().withMatcher("name", match -> match.contains());
	}

}

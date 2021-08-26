package io.cornerstone.core.web.controller.entity;

import static org.springframework.data.domain.Sort.Direction.DESC;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.web.AbstractEntityController;
import springfox.documentation.annotations.ApiIgnore;

@TestComponent
@RestController
@Validated
public class TestEntityController extends AbstractEntityController<TestEntity, Long> {

	public static final String PATH_LIST = "/testEntities";

	public static final String PATH_DETAIL = "/testEntity/{id:\\d+}";

	@Autowired
	private TestEntityRepository testEntityRepository;

	@Override
	@JsonView({ View.List.class })
	@GetMapping(PATH_LIST)
	public ResultPage<TestEntity> list(@PageableDefault(sort = "id", direction = DESC) Pageable pageable,
			@RequestParam(required = false) String query, @ApiIgnore TestEntity example) {
		return super.list(pageable, query, example);
	}

	@Override
	@PostMapping(PATH_LIST)
	public TestEntity save(@RequestBody @JsonView(View.Creation.class) @Valid TestEntity testEntity) {
		return super.save(testEntity);
	}

	@Override
	@GetMapping(PATH_DETAIL)
	public TestEntity get(@PathVariable Long id) {
		return super.get(id);
	}

	@Override
	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable Long id, @RequestBody @JsonView(View.Update.class) @Valid TestEntity testEntity) {
		super.update(id, testEntity);
	}

	@Override
	@PatchMapping(PATH_DETAIL)
	public TestEntity patch(@PathVariable Long id,
			@RequestBody @JsonView(View.Update.class) @Valid TestEntity testEntity) {
		return super.patch(id, testEntity);
	}

	@Override
	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable Long id) {
		super.delete(id);
	}

	@Override
	protected void beforeSave(TestEntity testEntity) {
		if (testEntityRepository.existsByIdNo(testEntity.getIdNo()))
			throw badRequest("idNo.already.exists");
	}

	@Override
	protected void beforeDelete(TestEntity testEntity) {
		if (testEntity.getDisabled() != Boolean.TRUE)
			throw badRequest("disable.before.delete");
	}

	@Override
	protected Specification<TestEntity> getQuerySpecification(String query) {
		String q = '%' + query + '%';
		return (root, cq, cb) -> cb.or(cb.or(cb.like(root.get("idNo"), q), cb.like(root.get("name"), q)),
				cb.equal(root.get("phone"), query));
	}

	@Override
	protected ExampleMatcher getExampleMatcher() {
		return ExampleMatcher.matching().withIgnorePaths("address")
				.withMatcher("idNo", match -> match.contains().ignoreCase())
				.withMatcher("name", match -> match.contains());
	}
}

package io.cornerstone.core.web.controller.entity;

import io.cornerstone.core.web.AbstractEntityController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
class TestEntityController extends AbstractEntityController<TestEntity, Long> {

	@Autowired
	private TestEntityRepository testEntityRepository;

	@Override
	protected void beforeSave(TestEntity testEntity) {
		if (this.testEntityRepository.existsByIdNo(testEntity.getIdNo())) {
			throw badRequest("idNo.already.exists");
		}
	}

	@Override
	protected void beforeDelete(TestEntity testEntity) {
		if (testEntity.getDisabled() != Boolean.TRUE) {
			throw badRequest("disable.before.delete");
		}
	}

	@Override
	protected Specification<TestEntity> getQuerySpecification(String query) {
		String q = '%' + query + '%';
		return (root, cq, cb) -> cb.or(cb.or(cb.like(root.get("idNo"), q), cb.like(root.get("name"), q)),
				cb.equal(root.get("phone"), query));
	}

	@Override
	protected ExampleMatcher getExampleMatcher() {
		return ExampleMatcher.matching()
			.withIgnorePaths("address")
			.withMatcher("idNo", match -> match.contains().ignoreCase())
			.withMatcher("name", ExampleMatcher.GenericPropertyMatcher::contains);
	}

}

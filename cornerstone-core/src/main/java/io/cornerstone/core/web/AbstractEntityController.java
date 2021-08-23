package io.cornerstone.core.web;

import static org.springframework.data.domain.Sort.Direction.DESC;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.hibernate.domain.AbstractEntity;
import springfox.documentation.annotations.ApiIgnore;

public abstract class AbstractEntityController<T extends AbstractEntity> extends EntityControllerBase<T, Long> {

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

}

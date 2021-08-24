package io.cornerstone.core.web.controller.treeable;

import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.List;

import javax.validation.Valid;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.domain.Pageable;
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
import io.cornerstone.core.web.AbstractTreeableEntityController;
import springfox.documentation.annotations.ApiIgnore;

@TestComponent
@RestController
@Validated
public class TestEntityController extends AbstractTreeableEntityController<TestEntity> {

	public static final String PATH_LIST = "/testTreeableEntities";

	public static final String PATH_DETAIL = "/testEntity/{id:\\d+}";

	public static final String PATH_CHILDREN = "/testEntity/{id:\\d+}/children";

	@Override
	@GetMapping(PATH_CHILDREN)
	@JsonView(View.List.class)
	public List<TestEntity> children(@PathVariable Long id, @RequestParam(required = false) String query,
			@ApiIgnore TestEntity example) {
		return super.children(id, query, example);
	}

	@Override
	@GetMapping(PATH_LIST)
	@JsonView(View.List.class)
	public ResultPage<TestEntity> list(@PageableDefault(sort = "id", direction = ASC) Pageable pageable,
			@RequestParam(required = false) String query, @ApiIgnore TestEntity example) {
		return super.list(pageable, query, example);
	}

	@Override
	@PostMapping(PATH_LIST)
	public TestEntity save(@RequestBody @Valid @JsonView(View.Creation.class) TestEntity testEntity) {
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

}

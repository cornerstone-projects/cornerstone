package io.cornerstone.core.web.controller.treeable;

import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.List;

import javax.validation.Valid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@ConditionalOnProperty(name = "TestTreeableEntityController.enabled", havingValue = "true")
@RestController
@Validated
public class TestTreeableEntityController extends AbstractTreeableEntityController<TestTreeableEntity> {

	public static final String PATH_LIST = "/testTreeableEntities";

	public static final String PATH_DETAIL = "/testTreeableEntity/{id:\\d+}";

	public static final String PATH_CHILDREN = "/testTreeableEntity/{id:\\d+}/children";

	@Override
	@GetMapping(PATH_CHILDREN)
	@JsonView(View.List.class)
	public List<TestTreeableEntity> children(@PathVariable Long id, @RequestParam(required = false) String query,
			@ApiIgnore TestTreeableEntity example) {
		return super.children(id, query, example);
	}

	@Override
	@GetMapping(PATH_LIST)
	@JsonView(View.List.class)
	public ResultPage<TestTreeableEntity> list(@PageableDefault(sort = "id", direction = ASC) Pageable pageable,
			@RequestParam(required = false) String query, @ApiIgnore TestTreeableEntity example) {
		return super.list(pageable, query, example);
	}

	@Override
	@PostMapping(PATH_LIST)
	public TestTreeableEntity save(
			@RequestBody @Valid @JsonView(View.Creation.class) TestTreeableEntity testTreeableEntity) {
		return super.save(testTreeableEntity);
	}

	@Override
	@GetMapping(PATH_DETAIL)
	public TestTreeableEntity get(@PathVariable Long id) {
		return super.get(id);
	}

	@Override
	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable Long id,
			@RequestBody @JsonView(View.Update.class) @Valid TestTreeableEntity testTreeableEntity) {
		super.update(id, testTreeableEntity);
	}

	@Override
	@PatchMapping(PATH_DETAIL)
	public TestTreeableEntity patch(@PathVariable Long id,
			@RequestBody @JsonView(View.Update.class) @Valid TestTreeableEntity testTreeableEntity) {
		return super.patch(id, testTreeableEntity);
	}

	@Override
	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable Long id) {
		super.delete(id);
	}

}

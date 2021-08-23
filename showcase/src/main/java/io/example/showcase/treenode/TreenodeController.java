package io.example.showcase.treenode;

import java.util.List;

import javax.validation.Valid;

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

import io.cornerstone.core.domain.View;
import io.cornerstone.core.web.AbstractTreeableEntityController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
public class TreenodeController extends AbstractTreeableEntityController<Treenode> {

	public static final String PATH_LIST = "/treenodes";

	public static final String PATH_DETAIL = "/treenode/{id:\\d+}";

	public static final String PATH_CHILDREN = "/treenode/{id:\\d+}/children";

	@Override
	@GetMapping(PATH_CHILDREN)
	@JsonView(View.List.class)
	public List<Treenode> children(@PathVariable Long id, @RequestParam(required = false) String query,
			@ApiIgnore Treenode example) {
		return super.children(id, query, example);
	}

	@Override
	@GetMapping(PATH_LIST)
	@JsonView(View.List.class)
	public List<Treenode> list(@RequestParam(required = false) String query, @ApiIgnore Treenode example) {
		return super.list(query, example);
	}

	@Override
	@PostMapping(PATH_LIST)
	public Treenode save(@RequestBody @Valid @JsonView(View.Creation.class) Treenode treenode) {
		return super.save(treenode);
	}

	@Override
	@GetMapping(PATH_DETAIL)
	public Treenode get(@PathVariable Long id) {
		return super.get(id);
	}

	@Override
	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable Long id, @RequestBody @JsonView(View.Update.class) @Valid Treenode treenode) {
		super.update(id, treenode);
	}

	@Override
	@PatchMapping(PATH_DETAIL)
	public Treenode patch(@PathVariable Long id, @RequestBody @JsonView(View.Update.class) @Valid Treenode treenode) {
		return super.patch(id, treenode);
	}

	@Override
	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable Long id) {
		super.delete(id);
	}

}

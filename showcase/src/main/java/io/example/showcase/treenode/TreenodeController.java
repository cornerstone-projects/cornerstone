package io.example.showcase.treenode;

import java.util.List;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
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
import io.cornerstone.core.util.BeanUtils;
import io.cornerstone.core.web.BaseRestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
public class TreenodeController extends BaseRestController {

	public static final String PATH_LIST = "/treenodes";

	public static final String PATH_DETAIL = "/treenode/{id:\\d+}";

	public static final String PATH_CHILDREN = "/treenode/{id:\\d+}/children";

	@Autowired
	private TreenodeRepository treenodeRepository;

	@GetMapping(PATH_CHILDREN)
	@JsonView(View.List.class)
	public List<Treenode> children(@PathVariable Long id, @RequestParam(required = false) String query,
			@ApiIgnore Treenode example) {
		Specification<Treenode> spec = (root, cq, cb) -> {
			Predicate p = (id == null || id < 1) ? cb.isNull(root.get("parent"))
					: cb.equal(root.get("parent").get("id"), id);
			if (StringUtils.hasText(query)) {
				p = cb.and(p, cb.like(root.get("name"), '%' + query + '%'));
			} else {
				ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("name", match -> match.contains());
				p = cb.and(p, QueryByExamplePredicateBuilder.getPredicate(root, cb, Example.of(example, matcher)));
			}
			return p;
		};
		return treenodeRepository.findAll(spec);
	}

	@GetMapping(PATH_LIST)
	@JsonView(View.List.class)
	public List<Treenode> list(@RequestParam(required = false) String query, @ApiIgnore Treenode example) {
		if (StringUtils.hasText(query)) {
			String q = '%' + query + '%';
			Specification<Treenode> spec = (root, cq, cb) -> cb.like(root.get("name"), q);
			return treenodeRepository.findAll(spec);
		} else {
			ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("name", match -> match.contains());
			return treenodeRepository.findAll(Example.of(example, matcher));
		}
	}

	@PostMapping(PATH_LIST)
	public Treenode save(@RequestBody @Valid @JsonView(View.Creation.class) Treenode treenode) {
		return treenodeRepository.save(treenode);
	}

	@GetMapping(PATH_DETAIL)
	public Treenode get(@PathVariable Long id) {
		return treenodeRepository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable Long id, @RequestBody @JsonView(View.Update.class) @Valid Treenode treenode) {
		treenodeRepository.findById(id).map(u -> {
			BeanUtils.copyPropertiesInJsonView(treenode, u, View.Update.class);
			return treenodeRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PatchMapping(PATH_DETAIL)
	public Treenode updatePartial(@PathVariable Long id,
			@RequestBody @JsonView(View.Update.class) @Valid Treenode treenode) {
		return treenodeRepository.findById(id).map(u -> {
			BeanUtils.copyNonNullProperties(treenode, u);
			return treenodeRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable Long id) {
		treenodeRepository.deleteById(id);
	}

}

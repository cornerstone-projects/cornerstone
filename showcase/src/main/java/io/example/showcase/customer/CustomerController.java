package io.example.showcase.customer;

import static org.springframework.data.domain.Sort.Direction.DESC;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@Validated
public class CustomerController extends AbstractEntityController<Customer, Long> {

	public static final String PATH_LIST = "/customers";

	public static final String PATH_DETAIL = "/customer/{id:\\d+}";

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	@JsonView({ View.List.class })
	@GetMapping(PATH_LIST)
	public ResultPage<Customer> list(@PageableDefault(sort = "id", direction = DESC) Pageable pageable,
			@RequestParam(required = false) String query, @ApiIgnore Customer example) {
		return super.list(pageable, query, example);
	}

	@Override
	@PostMapping(PATH_LIST)
	public Customer save(@RequestBody @JsonView(View.Creation.class) @Valid Customer customer) {
		return super.save(customer);
	}

	@Override
	@GetMapping(PATH_DETAIL)
	public Customer get(@PathVariable Long id) {
		return super.get(id);
	}

	@Override
	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable Long id, @RequestBody @JsonView(View.Update.class) @Valid Customer customer) {
		super.update(id, customer);
	}

	@Override
	@PatchMapping(PATH_DETAIL)
	public Customer patch(@PathVariable Long id, @RequestBody @JsonView(View.Update.class) @Valid Customer customer) {
		return super.patch(id, customer);
	}

	@Override
	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable Long id) {
		super.delete(id);
	}

	@Override
	protected void beforeSave(Customer customer) {
		if (customerRepository.existsByIdNo(customer.getIdNo()))
			throw badRequest("idNo.already.exists");
	}

	@Override
	protected void beforeDelete(Customer customer) {
		if (customer.getDisabled() != Boolean.TRUE)
			throw badRequest("disable.before.delete");
	}

	@Override
	protected Specification<Customer> getQuerySpecification(String query) {
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

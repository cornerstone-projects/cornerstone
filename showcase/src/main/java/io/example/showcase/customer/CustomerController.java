package io.example.showcase.customer;

import static org.springframework.data.domain.Sort.Direction.DESC;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
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

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.util.BeanUtils;
import io.cornerstone.core.web.BaseRestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
public class CustomerController extends BaseRestController {

	public static final String PATH_LIST = "/customers";

	public static final String PATH_DETAIL = "/customer/{id:\\d+}";

	@Autowired
	private CustomerRepository customerRepository;

	@JsonView({ View.List.class })
	@GetMapping(PATH_LIST)
	public ResultPage<Customer> list(@PageableDefault(sort = "id", direction = DESC) Pageable pageable,
			@RequestParam(required = false) String query, @ApiIgnore Customer example) {
		Page<Customer> page;
		if (StringUtils.hasText(query)) {
			String q = '%' + query + '%';
			Specification<Customer> spec = (root, cq, cb) -> cb.or(
					cb.or(cb.like(root.get("idNo"), q), cb.like(root.get("name"), q)),
					cb.equal(root.get("phone"), query));
			page = customerRepository.findAll(spec, pageable);
		} else {
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("address")
					.withMatcher("idNo", match -> match.contains().ignoreCase())
					.withMatcher("name", match -> match.contains());
			page = customerRepository.findAll(Example.of(example, matcher), pageable);
		}
		return ResultPage.of(page);
	}

	@PostMapping(PATH_LIST)
	public Customer save(@RequestBody @JsonView(View.Creation.class) @Valid Customer customer) {
		if (customerRepository.existsByIdNo(customer.getIdNo()))
			throw badRequest("idNo.already.exists");
		return customerRepository.save(customer);
	}

	@GetMapping(PATH_DETAIL)
	public Customer get(@PathVariable Long id) {
		return customerRepository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable Long id, @RequestBody @JsonView(View.Update.class) @Valid Customer customer) {
		customerRepository.findById(id).map(u -> {
			BeanUtils.copyPropertiesInJsonView(customer, u,
					customer.getVersion() == null ? View.Edit.class : View.Update.class);
			return customerRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PatchMapping(PATH_DETAIL)
	public Customer updatePartial(@PathVariable Long id,
			@RequestBody @JsonView(View.Update.class) @Valid Customer customer) {
		return customerRepository.findById(id).map(u -> {
			BeanUtils.copyNonNullProperties(customer, u);
			return customerRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable Long id) {
		Customer customer = customerRepository.findById(id).orElseThrow(() -> notFound(id));
		if (customer.getDisabled() != Boolean.TRUE)
			throw badRequest("disable.before.delete");
		customerRepository.delete(customer);
	}

}

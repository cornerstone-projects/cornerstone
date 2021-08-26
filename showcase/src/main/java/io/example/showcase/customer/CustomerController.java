package io.example.showcase.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import io.cornerstone.core.web.AbstractEntityController;

@RestController
@Validated
public class CustomerController extends AbstractEntityController<Customer, Long> {


	@Autowired
	private CustomerRepository customerRepository;

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

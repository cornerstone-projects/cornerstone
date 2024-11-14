package io.example.showcase.customer;

import io.cornerstone.core.web.AbstractEntityController;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import static io.example.showcase.customer.Customer_.*;

@RequiredArgsConstructor
@RestController
public class CustomerController extends AbstractEntityController<Customer, Long> {

	private final CustomerRepository customerRepository;

	@Override
	protected void beforeSave(Customer customer) {
		if (this.customerRepository.existsByIdNo(customer.getIdNo())) {
			throw badRequest("idNo.already.exists");
		}
	}

	@Override
	protected void beforeDelete(Customer customer) {
		if (customer.getDisabled() != Boolean.TRUE) {
			throw badRequest("disable.before.delete");
		}
	}

	@Override
	protected Specification<Customer> getQuerySpecification(String query) {
		String q = '%' + query + '%';
		return (root, cq, cb) -> cb.or(cb.or(cb.like(root.get(idNo), q), cb.like(root.get(name), q)),
				cb.equal(root.get(phone), query));
	}

	@Override
	protected ExampleMatcher getExampleMatcher() {
		return ExampleMatcher.matching()
			.withIgnorePaths(ADDRESS)
			.withMatcher(ID_NO, match -> match.contains().ignoreCase())
			.withMatcher(NAME, ExampleMatcher.GenericPropertyMatcher::contains);
	}

}

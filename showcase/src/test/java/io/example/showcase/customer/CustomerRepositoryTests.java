package io.example.showcase.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.cornerstone.core.validation.validators.MobilePhoneNumberValidator;
import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = CustomerRepository.class)
@EntityScan(basePackageClasses = Customer.class)
public class CustomerRepositoryTests extends DataJpaTestBase {

	@Autowired
	CustomerRepository repository;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void save() {
		Customer customer = new Customer();
		customer.setName("name");
		assertThatThrownBy(() -> repository.save(customer)).isInstanceOf(DataIntegrityViolationException.class);
		customer.setIdNo("test");
		customer.setPhone("123");
		assertThatThrownBy(() -> repository.save(customer)).getRootCause()
				.isInstanceOf(ConstraintViolationException.class);
		customer.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		assertThatThrownBy(() -> repository.save(customer)).getRootCause()
				.isInstanceOf(ConstraintViolationException.class);
		customer.setPhone(MobilePhoneNumberValidator.randomValue());
		Customer savedCustomer = repository.save(customer);
		savedCustomer = repository.findById(savedCustomer.getId()).orElseThrow(IllegalStateException::new);
		assertThat(savedCustomer.getName()).isEqualTo(customer.getName());
		assertThat(savedCustomer.getIdNo()).isEqualTo(customer.getIdNo());
		assertThat(savedCustomer.getPhone()).isEqualTo(customer.getPhone());
		assertThat(savedCustomer.getCreatedDate()).isNotNull();
		repository.delete(savedCustomer);
	}

}

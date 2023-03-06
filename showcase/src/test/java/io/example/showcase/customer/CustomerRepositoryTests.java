package io.example.showcase.customer;

import javax.validation.ConstraintViolationException;

import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.cornerstone.core.validation.validators.MobilePhoneNumberValidator;
import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@EnableJpaRepositories(basePackageClasses = CustomerRepository.class)
@EntityScan(basePackageClasses = Customer.class)
class CustomerRepositoryTests extends DataJpaTestBase {

	@Autowired
	CustomerRepository repository;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void save() {
		Customer customer = new Customer();
		customer.setName("name");
		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> this.repository.save(customer));
		customer.setIdNo("test");
		customer.setPhone("123");
		assertThatExceptionOfType(TransactionException.class).isThrownBy(() -> this.repository.save(customer))
			.withRootCauseInstanceOf(ConstraintViolationException.class);
		customer.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		assertThatExceptionOfType(TransactionException.class).isThrownBy(() -> this.repository.save(customer))
			.withRootCauseInstanceOf(ConstraintViolationException.class);
		customer.setPhone(MobilePhoneNumberValidator.randomValue());
		Customer savedCustomer = this.repository.save(customer);
		Long id = savedCustomer.getId();
		assertThat(id).isNotNull();
		savedCustomer = this.repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(savedCustomer.getName()).isEqualTo(customer.getName());
		assertThat(savedCustomer.getIdNo()).isEqualTo(customer.getIdNo());
		assertThat(savedCustomer.getPhone()).isEqualTo(customer.getPhone());
		assertThat(savedCustomer.getCreatedDate()).isNotNull();
		this.repository.delete(savedCustomer);
	}

}

package io.example.showcase.customer;

import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.cornerstone.core.validation.validators.MobilePhoneNumberValidator;
import io.cornerstone.test.DataJpaTestBase;
import jakarta.validation.ConstraintViolationException;
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
		Customer c1 = new Customer();
		c1.setName("name");
		assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(() -> this.repository.save(c1));
		Customer c2 = new Customer();
		c1.setName("name");
		c2.setIdNo("test");
		c2.setPhone("123");
		assertThatExceptionOfType(TransactionException.class).isThrownBy(() -> this.repository.save(c2))
			.withRootCauseInstanceOf(ConstraintViolationException.class);
		Customer c3 = new Customer();
		c3.setName("name");
		c3.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c3.setPhone("123");
		assertThatExceptionOfType(TransactionException.class).isThrownBy(() -> this.repository.save(c3))
			.withRootCauseInstanceOf(ConstraintViolationException.class);
		Customer c4 = new Customer();
		c4.setName("name");
		c4.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c4.setPhone(MobilePhoneNumberValidator.randomValue());
		Customer savedCustomer = this.repository.save(c4);
		Long id = savedCustomer.getId();
		assertThat(id).isNotNull();
		savedCustomer = this.repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(savedCustomer.getName()).isEqualTo(c4.getName());
		assertThat(savedCustomer.getIdNo()).isEqualTo(c4.getIdNo());
		assertThat(savedCustomer.getPhone()).isEqualTo(c4.getPhone());
		assertThat(savedCustomer.getCreatedDate()).isNotNull();
		this.repository.delete(savedCustomer);
	}

}

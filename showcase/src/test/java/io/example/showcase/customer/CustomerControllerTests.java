package io.example.showcase.customer;

import java.util.ArrayList;
import java.util.List;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.validation.validators.CitizenIdentificationNumberValidator;
import io.example.showcase.BaseControllerTests;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

class CustomerControllerTests extends BaseControllerTests {

	private static final String PATH_LIST = "/customers";

	private static final String PATH_DETAIL = "/customer/{id}";

	@Test
	void crud() {
		TestRestTemplate restTemplate = adminRestTemplate();
		Customer c = new Customer();
		c.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c.setName("test");
		c.setDisabled(Boolean.TRUE);

		// create
		ResponseEntity<Customer> response = restTemplate.postForEntity(PATH_LIST, c, Customer.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		Customer customer = response.getBody();
		assertThat(customer).isNotNull();
		assertThat(customer.getId()).isNotNull();
		assertThat(customer.getIdNo()).isEqualTo(c.getIdNo());
		assertThat(customer.getName()).isEqualTo(c.getName());
		assertThat(customer.getDisabled()).isEqualTo(c.getDisabled());
		Long id = customer.getId();

		// read
		response = restTemplate.getForEntity(PATH_DETAIL, Customer.class, id);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(customer);

		// update partial
		Customer c2 = new Customer();
		c2.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c2.setName("new name");
		c2.setDisabled(Boolean.TRUE);
		Customer c3 = restTemplate.patchForObject(PATH_DETAIL, c2, Customer.class, id);
		assertThat(c3.getName()).isEqualTo(c2.getName());
		assertThat(c3.getDisabled()).isEqualTo(c2.getDisabled());
		assertThat(c3.getIdNo()).isEqualTo(customer.getIdNo()); // idNo not updatable
		// update full
		c3.setIdNo(CitizenIdentificationNumberValidator.randomValue());
		c3.setName("name");
		c3.setDisabled(Boolean.FALSE);
		restTemplate.put(PATH_DETAIL, c3, id);
		Customer c4 = restTemplate.getForEntity(PATH_DETAIL, Customer.class, id).getBody();
		assertThat(c4).isNotNull();
		assertThat(c4.getDisabled()).isEqualTo(c3.getDisabled());
		assertThat(c4.getName()).isEqualTo(c3.getName());
		assertThat(c4.getIdNo()).isEqualTo(customer.getIdNo()); // idNo not updatable

		// delete
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, id).build(), void.class)
			.getStatusCode()).isSameAs(BAD_REQUEST);
		c4.setDisabled(Boolean.TRUE);
		restTemplate.put(PATH_DETAIL, c4, id);
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, id).build(), void.class)
			.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, Customer.class, id).getStatusCode()).isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = adminRestTemplate();
		int size = 5;
		List<Customer> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Customer c = new Customer();
			c.setIdNo(CitizenIdentificationNumberValidator.randomValue());
			c.setName("test" + i);
			c.setDisabled(Boolean.TRUE);
			list.add(restTemplate.postForObject(PATH_LIST, c, Customer.class));
		}

		ResponseEntity<ResultPage<Customer>> response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_LIST).build(), new ParameterizedTypeReference<ResultPage<Customer>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		ResultPage<Customer> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(size);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(size);
		assertThat(page.getResult().getFirst().getCreatedDate()).isNull(); // Customer.View.List
																			// view
		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?page=2&size=1&sort=id,desc").build(),
				new ParameterizedTypeReference<ResultPage<Customer>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(2);
		assertThat(page.getSize()).isEqualTo(1);

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?query=test0").build(),
				new ParameterizedTypeReference<ResultPage<Customer>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);

		ResponseEntity<ResultPage<Customer>> response2 = restTemplate.exchange(
				RequestEntity.method(GET, PATH_LIST + "?name=test0").build(),
				new ParameterizedTypeReference<ResultPage<Customer>>() {
				});
		assertThat(response2.getBody()).isEqualTo(response.getBody());

		for (Customer c : list) {
			restTemplate.delete(PATH_DETAIL, c.getId());
		}
	}

}

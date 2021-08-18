package io.example.showcase.treenode;

import static io.example.showcase.treenode.TreenodeController.PATH_DETAIL;
import static io.example.showcase.treenode.TreenodeController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import io.example.showcase.BaseControllerTests;

class TreenodeControllerTests extends BaseControllerTests {

	@Test
	void crud() {
		TestRestTemplate restTemplate = adminRestTemplate();
		Treenode parent = new Treenode();
		parent.setName("parent");

		// create
		ResponseEntity<Treenode> response = restTemplate.postForEntity(PATH_LIST, parent, Treenode.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		Treenode treenode = response.getBody();
		assertThat(treenode).isNotNull();
		assertThat(treenode.getId()).isNotNull();
		assertThat(treenode.getName()).isEqualTo(parent.getName());
		assertThat(treenode.getLevel()).isEqualTo(1);
		Treenode child = new Treenode();
		child.setName("child");
		child.setParent(treenode);
		response = restTemplate.postForEntity(PATH_LIST, child, Treenode.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		child = response.getBody();
		assertThat(child).isNotNull();
		assertThat(child.getId()).isNotNull();
		assertThat(child.getName()).isEqualTo("child");
		assertThat(child.getLevel()).isEqualTo(2);
		assertThat(child.getParent()).isNotNull();
		assertThat(child.getParent().getId()).isEqualTo(treenode.getId());

		// read
		response = restTemplate.getForEntity(PATH_DETAIL, Treenode.class, treenode.getId());
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(treenode);

		// update partial
		Treenode temp = new Treenode();
		temp.setName("new name");
		child = restTemplate.patchForObject(PATH_DETAIL, temp, Treenode.class, child.getId());
		assertThat(child.getName()).isEqualTo(temp.getName());
		assertThat(child.getParent()).isNotNull(); // parent not updated
		assertThat(child.getLevel()).isEqualTo(2);
		// update full
		temp.setName("name");
		temp.setParent(null);
		restTemplate.put(PATH_DETAIL, temp, child.getId());
		child = restTemplate.getForEntity(PATH_DETAIL, Treenode.class, child.getId()).getBody();
		assertThat(child).isNotNull();
		assertThat(child.getName()).isEqualTo("name");
		assertThat(child.getParent()).isNull();
		assertThat(child.getLevel()).isEqualTo(1);

		// delete
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, child.getId()).build(), void.class)
				.getStatusCode()).isSameAs(OK);
		assertThat(
				restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, treenode.getId()).build(), void.class)
						.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, Treenode.class, treenode.getId()).getStatusCode())
				.isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = adminRestTemplate();
		int size = 5;
		List<Treenode> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Treenode c = new Treenode();
			c.setName("test" + i);
			list.add(restTemplate.postForObject(PATH_LIST, c, Treenode.class));
		}

		ResponseEntity<List<Treenode>> response = restTemplate.exchange(
				RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<List<Treenode>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		List<Treenode> result = response.getBody();
		assertThat(result).hasSize(size);
	}

}

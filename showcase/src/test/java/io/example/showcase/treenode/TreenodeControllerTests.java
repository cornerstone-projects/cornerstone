package io.example.showcase.treenode;

import java.util.List;

import io.cornerstone.core.domain.ResultPage;
import io.example.showcase.BaseControllerTests;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

class TreenodeControllerTests extends BaseControllerTests {

	private static final String PATH_LIST = "/treenodes";

	private static final String PATH_DETAIL = "/treenode/{id}";

	private static final String PATH_CHILDREN = PATH_DETAIL + "/children";

	@Test
	void crud() {
		TestRestTemplate restTemplate = adminRestTemplate();

		// create
		Treenode parent = new Treenode("parent");
		ResponseEntity<Treenode> response = restTemplate.postForEntity(PATH_LIST, parent, Treenode.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		Treenode treenode = response.getBody();
		assertThat(treenode).isNotNull();
		assertThat(treenode.getId()).isNotNull();
		assertThat(treenode.getName()).isEqualTo(parent.getName());
		assertThat(treenode.getLevel()).isEqualTo(1);

		assertThat(
				restTemplate.postForEntity(PATH_LIST, new Treenode(treenode.getName()), Treenode.class).getStatusCode())
			.isSameAs(BAD_REQUEST); // name already exists

		Treenode child = new Treenode("child");
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
		assertThat(restTemplate
			.postForEntity(PATH_LIST, new Treenode(child.getParent(), child.getName(), 0), Treenode.class)
			.getStatusCode()).isSameAs(BAD_REQUEST);

		Treenode child2 = new Treenode("child2");
		child2.setParent(child.getParent());
		child2 = restTemplate.postForObject(PATH_LIST, child2, Treenode.class);
		assertThat(child2.getId()).isNotNull();

		// read
		response = restTemplate.getForEntity(PATH_DETAIL, Treenode.class, treenode.getId());
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(treenode);

		// update partial
		Treenode temp = new Treenode("new name");
		child = restTemplate.patchForObject(PATH_DETAIL, temp, Treenode.class, child.getId());
		assertThat(child.getName()).isEqualTo(temp.getName());
		assertThat(child.getParent()).isNotNull(); // parent not updated
		assertThat(child.getLevel()).isEqualTo(2);
		assertThat(restTemplate
			.exchange(RequestEntity.method(PATCH, PATH_DETAIL, child2.getId())
				.body(new Treenode(child.getParent(), child.getName(), 0)), Treenode.class)
			.getStatusCode()).isSameAs(BAD_REQUEST); // name already exists

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
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, child2.getId()).build(), void.class)
			.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, child.getId()).build(), void.class)
			.getStatusCode()).isSameAs(OK);
		assertThat(
				restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, treenode.getId()).build(), void.class)
					.getStatusCode())
			.isSameAs(OK);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, Treenode.class, treenode.getId()).getStatusCode())
			.isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = adminRestTemplate();
		Treenode parent1 = restTemplate.postForObject(PATH_LIST, new Treenode("parent1", 1), Treenode.class);
		Treenode parent2 = restTemplate.postForObject(PATH_LIST, new Treenode("parent2", 2), Treenode.class);
		Treenode child1 = restTemplate.postForObject(PATH_LIST, new Treenode(parent1, "child1", 1), Treenode.class);
		Treenode child2 = restTemplate.postForObject(PATH_LIST, new Treenode(parent1, "child2", 1), Treenode.class);

		ResponseEntity<ResultPage<Treenode>> response = restTemplate
			.exchange(RequestEntity.method(GET, PATH_LIST).build(), new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isSameAs(OK);

		ResultPage<Treenode> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(4);

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?query=child").build(),
				new ParameterizedTypeReference<>() {
				});
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?name=child").build(),
				new ParameterizedTypeReference<>() {
				});
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?level=2").build(),
				new ParameterizedTypeReference<>() {
				});
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);

		restTemplate.delete(PATH_DETAIL, child1.getId());
		restTemplate.delete(PATH_DETAIL, child2.getId());
		restTemplate.delete(PATH_DETAIL, parent1.getId());
		restTemplate.delete(PATH_DETAIL, parent2.getId());
	}

	@Test
	void children() {
		TestRestTemplate restTemplate = adminRestTemplate();
		Treenode parent1 = restTemplate.postForObject(PATH_LIST, new Treenode("parent1", 1), Treenode.class);
		Treenode parent2 = restTemplate.postForObject(PATH_LIST, new Treenode("parent2", 2), Treenode.class);
		Treenode child1 = restTemplate.postForObject(PATH_LIST, new Treenode(parent1, "child1", 1), Treenode.class);
		Treenode child2 = restTemplate.postForObject(PATH_LIST, new Treenode(parent1, "child2", 1), Treenode.class);

		ResponseEntity<List<Treenode>> response = restTemplate
			.exchange(RequestEntity.method(GET, PATH_CHILDREN, 0).build(), new ParameterizedTypeReference<>() {
			});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(parent2.getName());

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_CHILDREN, parent1.getId()).build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(child2.getName());

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_CHILDREN + "?query=parent", 0).build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(parent1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(parent2.getName());

		response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN + "?query=" + parent2.getName(), 0).build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(parent2.getName());

		response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN + "?query=child", parent1.getId()).build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(child1.getName());
		assertThat(response.getBody()).element(1).extracting("name").isEqualTo(child2.getName());

		response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_CHILDREN + "?query=" + child2.getName(), parent1.getId()).build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()).element(0).extracting("name").isEqualTo(child2.getName());

		restTemplate.delete(PATH_DETAIL, child1.getId());
		restTemplate.delete(PATH_DETAIL, child2.getId());
		restTemplate.delete(PATH_DETAIL, parent1.getId());
		restTemplate.delete(PATH_DETAIL, parent2.getId());
	}

}

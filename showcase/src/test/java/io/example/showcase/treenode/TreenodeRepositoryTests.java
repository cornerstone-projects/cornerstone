package io.example.showcase.treenode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TreenodeRepository.class)
@EntityScan(basePackageClasses = Treenode.class)
class TreenodeRepositoryTests extends DataJpaTestBase {

	@Autowired
	TreenodeRepository repository;

	@Test
	void save() {
		Treenode treenode = new Treenode("parent");
		repository.saveAndFlush(treenode);

		assertThat(treenode.getName()).isEqualTo(treenode.getName());
		assertThat(treenode.getLevel()).isEqualTo(1);
		assertThat(treenode.getFullId()).isEqualTo("1.");

	}

}

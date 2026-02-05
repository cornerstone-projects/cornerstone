package io.example.showcase.treenode;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TreenodeRepository.class)
@EntityScan(basePackageClasses = Treenode.class)
class TreenodeRepositoryTests extends DataJpaTestBase {

	@Autowired
	TreenodeRepository repository;

	@Test
	void save() {
		Treenode treenode = new Treenode("parent");
		this.repository.saveAndFlush(treenode);

		assertThat(treenode.getLevel()).isEqualTo(1);
		assertThat(treenode.getFullId()).isEqualTo("1.");

	}

}

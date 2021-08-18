package io.cornerstone.core.repository.treeable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
public class TreeableRepositoryTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	public void save() {
		TestEntity parent = new TestEntity("parent");
		TestEntity child0 = new TestEntity("child0", 0);
		TestEntity child1 = new TestEntity("child1", 1);
		TestEntity grandchild0 = new TestEntity("grandchild0", 0);
		TestEntity grandchild1 = new TestEntity("grandchild1", 1);
		parent.addChild(child0);
		parent.addChild(child1);
		child0.addChild(grandchild0);
		child0.addChild(grandchild1);
		repository.save(parent);
		repository.save(child0);
		repository.save(child1);
		repository.save(grandchild0);
		repository.save(grandchild1);
		flushAndClear();

		Long id = parent.getId();
		assertThat(id).isNotNull();
		TestEntity savedParent = repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(savedParent.getName()).isEqualTo(parent.getName());
		assertThat(savedParent.getLevel()).isEqualTo(1);
		assertThat(savedParent.getFullId()).isEqualTo("1.");
		Collection<TestEntity> children = savedParent.getChildren();
		assertThat(children).isNotNull();
		assertThat(children).hasSize(2);
		Iterator<TestEntity> it = children.iterator();
		TestEntity savedChild0 = it.next();
		TestEntity savedChild1 = it.next();
		assertThat(savedChild0.getName()).isEqualTo(child0.getName());
		assertThat(savedChild0.getLevel()).isEqualTo(2);
		assertThat(savedChild0.getFullId()).isEqualTo("1.2.");
		assertThat(savedChild1.getName()).isEqualTo(child1.getName());
		assertThat(savedChild1.getLevel()).isEqualTo(2);
		assertThat(savedChild1.getFullId()).isEqualTo("1.3.");
		children = savedChild0.getChildren();
		assertThat(children).isNotNull();
		assertThat(children).hasSize(2);
		it = children.iterator();
		TestEntity savedGrandchild0 = it.next();
		TestEntity savedGrandchild1 = it.next();
		assertThat(savedGrandchild0.getName()).isEqualTo(grandchild0.getName());
		assertThat(savedGrandchild0.getLevel()).isEqualTo(3);
		assertThat(savedGrandchild0.getFullId()).isEqualTo("1.2.4.");
		assertThat(savedGrandchild1.getName()).isEqualTo(grandchild1.getName());
		assertThat(savedGrandchild1.getLevel()).isEqualTo(3);
		assertThat(savedGrandchild1.getFullId()).isEqualTo("1.2.5.");

		savedChild0.setParent(savedChild1);
		repository.save(savedChild0);
		flushAndClear();
		id = savedChild0.getId();
		assertThat(id).isNotNull();
		savedChild0 = repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(savedChild0.getName()).isEqualTo(child0.getName());
		assertThat(savedChild0.getLevel()).isEqualTo(3);
		assertThat(savedChild0.getFullId()).isEqualTo("1.3.2.");
		children = savedChild0.getChildren();
		assertThat(children).isNotNull();
		assertThat(children).hasSize(2);
		it = children.iterator();
		savedGrandchild0 = it.next();
		savedGrandchild1 = it.next();
		assertThat(savedGrandchild0.getName()).isEqualTo(grandchild0.getName());
		assertThat(savedGrandchild0.getLevel()).isEqualTo(4);
		assertThat(savedGrandchild0.getFullId()).isEqualTo("1.3.2.4.");
		assertThat(savedGrandchild1.getName()).isEqualTo(grandchild1.getName());
		assertThat(savedGrandchild1.getLevel()).isEqualTo(4);
		assertThat(savedGrandchild1.getFullId()).isEqualTo("1.3.2.5.");
	}

}

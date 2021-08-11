package io.cornerstone.core.hibernate.id.snowflake;

import org.springframework.data.repository.CrudRepository;

public interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}

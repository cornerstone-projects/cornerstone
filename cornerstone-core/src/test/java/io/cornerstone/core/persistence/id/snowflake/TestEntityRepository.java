package io.cornerstone.core.persistence.id.snowflake;

import org.springframework.data.repository.CrudRepository;

interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}

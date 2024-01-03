package io.cornerstone.core.persistence.event;

import org.springframework.data.repository.CrudRepository;

interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}

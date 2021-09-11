package io.cornerstone.core.hibernate.event;

import org.springframework.data.repository.CrudRepository;

interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}

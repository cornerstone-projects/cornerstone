package io.cornerstone.core.hibernate.id.sequence;

import org.springframework.data.repository.CrudRepository;

interface TestEntityRepository extends CrudRepository<TestEntity, Long> {

}

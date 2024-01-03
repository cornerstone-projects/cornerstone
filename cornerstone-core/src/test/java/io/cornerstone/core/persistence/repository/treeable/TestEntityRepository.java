package io.cornerstone.core.persistence.repository.treeable;

import io.cornerstone.core.persistence.repository.TreeableRepository;

import org.springframework.data.repository.CrudRepository;

interface TestEntityRepository extends CrudRepository<TestEntity, Long>, TreeableRepository<TestEntity> {

}

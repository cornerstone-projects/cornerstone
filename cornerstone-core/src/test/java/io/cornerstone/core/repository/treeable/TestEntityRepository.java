package io.cornerstone.core.repository.treeable;

import io.cornerstone.core.repository.TreeableRepository;

import org.springframework.data.repository.CrudRepository;

interface TestEntityRepository extends CrudRepository<TestEntity, Long>, TreeableRepository<TestEntity> {

}

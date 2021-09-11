package io.cornerstone.core.repository.treeable;

import org.springframework.data.repository.CrudRepository;

import io.cornerstone.core.repository.TreeableRepository;

interface TestEntityRepository extends CrudRepository<TestEntity, Long>, TreeableRepository<TestEntity> {

}

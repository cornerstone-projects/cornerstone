package io.cornerstone.core.web.controller.treeable;

import io.cornerstone.core.repository.TreeableRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface TestEntityRepository
		extends JpaRepository<TestEntity, Long>, JpaSpecificationExecutor<TestEntity>, TreeableRepository<TestEntity> {

}

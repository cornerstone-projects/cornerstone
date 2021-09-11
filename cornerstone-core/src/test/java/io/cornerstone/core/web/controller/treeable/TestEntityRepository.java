package io.cornerstone.core.web.controller.treeable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.cornerstone.core.repository.TreeableRepository;

interface TestEntityRepository
		extends JpaRepository<TestEntity, Long>, JpaSpecificationExecutor<TestEntity>, TreeableRepository<TestEntity> {

}

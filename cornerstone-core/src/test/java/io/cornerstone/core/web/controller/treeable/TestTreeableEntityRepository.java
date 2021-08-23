package io.cornerstone.core.web.controller.treeable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.cornerstone.core.repository.TreeableRepository;

public interface TestTreeableEntityRepository
		extends JpaRepository<TestTreeableEntity, Long>, JpaSpecificationExecutor<TestTreeableEntity>, TreeableRepository<TestTreeableEntity> {

}

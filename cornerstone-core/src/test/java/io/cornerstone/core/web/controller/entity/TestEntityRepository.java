package io.cornerstone.core.web.controller.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestEntityRepository extends JpaRepository<TestEntity, Long>, JpaSpecificationExecutor<TestEntity> {

	boolean existsByIdNo(String idNo);

}

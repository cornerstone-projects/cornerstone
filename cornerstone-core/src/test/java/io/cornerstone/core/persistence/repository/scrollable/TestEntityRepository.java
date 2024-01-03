package io.cornerstone.core.persistence.repository.scrollable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface TestEntityRepository extends JpaRepository<TestEntity, Long>, JpaSpecificationExecutor<TestEntity> {

}

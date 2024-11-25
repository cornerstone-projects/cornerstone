package io.cornerstone.core.persistence.repository.streamable;

import io.cornerstone.core.persistence.repository.BeanProviderCapable;
import io.cornerstone.core.persistence.repository.StreamableJpaRepository;

interface TestEntityRepository extends StreamableJpaRepository<TestEntity, Long>, BeanProviderCapable {

}

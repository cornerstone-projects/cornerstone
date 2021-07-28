package io.cornerstone.core.hibernate.audit;

import org.springframework.data.repository.CrudRepository;

public interface AuditableEntityRepository extends CrudRepository<AuditableEntity, Long> {

}

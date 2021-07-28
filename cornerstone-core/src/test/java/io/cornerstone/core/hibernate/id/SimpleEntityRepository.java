package io.cornerstone.core.hibernate.id;

import org.springframework.data.repository.CrudRepository;

public interface SimpleEntityRepository extends CrudRepository<SimpleEntity, Long> {

}

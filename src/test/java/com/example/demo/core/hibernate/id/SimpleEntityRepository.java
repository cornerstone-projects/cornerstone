package com.example.demo.core.hibernate.id;

import org.springframework.data.repository.CrudRepository;

public interface SimpleEntityRepository extends CrudRepository<SimpleEntity, Long> {

}

package io.cornerstone.core.persistence.repository.beanprovidercapable;

import jakarta.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
class TestEntity extends AbstractPersistable<Long> {

}

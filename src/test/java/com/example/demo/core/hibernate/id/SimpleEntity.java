package com.example.demo.core.hibernate.id;

import javax.persistence.Entity;

import com.example.demo.core.hibernate.domain.AbstractEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SimpleEntity extends AbstractEntity {

	private static final long serialVersionUID = 6471017006033411659L;

}
package com.example.demo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NamedQuery(name = "User.findByTheUsersName", query = "from User u where u.username = ?1")
public class User extends AbstractPersistable<Long> {

	@Column(unique = true)
	private String username;

	private String firstname;

	private String lastname;

	public User() {
	}

	public User(Long id) {
		this.setId(id);
	}

}
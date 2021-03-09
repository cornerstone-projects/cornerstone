package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

	User findByTheUsersName(String username);

	List<User> findByLastname(String lastname);

	@Query("select u from User u where u.firstname = :firstname")
	List<User> findByFirstname(String firstname);
}

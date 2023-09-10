package com.smart.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.smart.entities.User;

@Component
public interface UserRepository extends CrudRepository<User, Integer> {

	@Query("select u from User u where u.uemail = :uemail")
	public User getUserByUserName(@Param("uemail") String uemail);
}

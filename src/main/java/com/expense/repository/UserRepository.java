package com.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expense.model.User;

public interface UserRepository extends JpaRepository<User,Long> {
	User findByEmail(String email);
}

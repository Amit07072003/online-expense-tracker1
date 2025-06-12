package com.expense.service;

import com.expense.model.User;

public interface UserService {
    void register(User user);
    User login(String email, String password);
    User findByEmail(String email);
}

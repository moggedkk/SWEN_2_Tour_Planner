package com.backend.backend.service.declaration;

import com.backend.backend.model.entity.User;

public interface IUserService {
    User registerUser(User user);
    User findUserByUsername(String username);
    void deleteUser(User user);
}

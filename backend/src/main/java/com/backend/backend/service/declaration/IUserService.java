package com.backend.backend.service.declaration;

import com.backend.backend.model.dto.RegisterRequest;
import com.backend.backend.model.entity.User;

import java.util.Optional;

public interface IUserService {
    User registerUser(RegisterRequest request);
    Optional<User> findUserByUsername(String username);
    void deleteUser(User user);
}

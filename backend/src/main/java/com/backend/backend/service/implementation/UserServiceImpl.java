package com.backend.backend.service.implementation;

import com.backend.backend.model.LoginRequest;
import com.backend.backend.model.User;
import com.backend.backend.service.declaration.IUserService;
import org.springframework.stereotype.Service;
@Service
public class UserServiceImpl implements IUserService {
    @Override
    public User registerUser(User user) {

        return user;
    }

    @Override
    public User findUserByUsername(String username) {
        return null;
    }

    @Override
    public void deleteUser(User user) {

    }


}

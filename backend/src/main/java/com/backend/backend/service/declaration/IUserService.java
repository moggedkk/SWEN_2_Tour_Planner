package com.backend.backend.service.declaration;

import com.backend.backend.model.LoginRequest;
import com.backend.backend.model.User;

public interface IUserService {
    void registerUser(User user);
    void loginUser(LoginRequest logReq);
}

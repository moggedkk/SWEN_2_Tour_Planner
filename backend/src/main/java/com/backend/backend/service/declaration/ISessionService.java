package com.backend.backend.service.declaration;

import com.backend.backend.model.LoginRequest;
import com.backend.backend.model.User;

public interface ISessionService {
    public String login(LoginRequest loginRequest);
    public void logout();
    public void refreshToken(User user);
}

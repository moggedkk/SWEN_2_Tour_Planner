package com.backend.backend.service.declaration;

import com.backend.backend.model.dto.LoginRequest;
import com.backend.backend.model.entity.User;

public interface ISessionService {
    public String login(LoginRequest loginRequest);
    public void refreshToken(User user);
}

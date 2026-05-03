package com.backend.backend.service.implementation;

import com.backend.backend.model.dto.LoginRequest;
import com.backend.backend.model.entity.User;
import com.backend.backend.service.declaration.ISessionService;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl implements ISessionService {
    @Override
    public String login(LoginRequest loginRequest) {

        return "";
    }

    @Override
    public void logout() {

    }

    @Override
    public void refreshToken(User user) {

    }
}

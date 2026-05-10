package com.backend.backend.service.implementation;

import com.backend.backend.exception.InvalidCredentialsException;
import com.backend.backend.model.dto.LoginRequest;
import com.backend.backend.model.entity.User;
import com.backend.backend.security.jwt.JwtConfig;
import com.backend.backend.service.declaration.ISessionService;
import com.backend.backend.service.declaration.IUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl implements ISessionService {

    private final IUserService userService;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;

    public SessionServiceImpl(IUserService userService, JwtConfig jwtConfig, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtConfig = jwtConfig;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String login(LoginRequest loginRequest) {
        User user = userService.findUserByUsername(loginRequest.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getHash())) {
            throw new InvalidCredentialsException();
        }

        return jwtConfig.createJWT(user.getUsername());
    }

    @Override
    public void logout() {
        // Token invalidation is a client-side concern with stateless JWT.
        // Implement a token denylist here if revocation is needed in the future.
    }

    @Override
    public void refreshToken(User user) {
        // Not yet implemented.
    }
}

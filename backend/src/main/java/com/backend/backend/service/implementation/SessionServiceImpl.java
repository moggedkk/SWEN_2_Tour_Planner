package com.backend.backend.service.implementation;

import com.backend.backend.exception.InvalidCredentialsException;
import com.backend.backend.model.dto.LoginRequest;
import com.backend.backend.model.entity.User;
import com.backend.backend.security.jwt.JwtConfig;
import com.backend.backend.service.declaration.ISessionService;
import com.backend.backend.service.declaration.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
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
        log.debug("Looking up user '{}'", loginRequest.getUsername());
        // findUserByUsername returns an Optional<User>, orElseThrow unwraps it or throws if empty
        User user = userService.findUserByUsername(loginRequest.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getHash())) {
            log.warn("Login failed: wrong password for user '{}'", loginRequest.getUsername());
            throw new InvalidCredentialsException();
        }

        log.info("User '{}' logged in successfully", loginRequest.getUsername());
        return jwtConfig.createJWT(user.getUsername());
    }


    @Override
    public void refreshToken(User user) {
        // Not yet implemented. Maybe at the end
    }
}

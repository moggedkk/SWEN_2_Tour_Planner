package com.backend.backend.service;

import com.backend.backend.exception.InvalidCredentialsException;
import com.backend.backend.model.dto.LoginRequest;
import com.backend.backend.model.entity.User;
import com.backend.backend.security.jwt.JwtConfig;
import com.backend.backend.service.declaration.IUserService;
import com.backend.backend.service.implementation.SessionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock private IUserService userService;
    @Mock private JwtConfig jwtConfig;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void login_correctCredentials_returnsToken() {
        User user = new User();
        user.setUsername("alice");
        user.setHash("hashed");

        when(userService.findUserByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtConfig.createJWT("alice")).thenReturn("mocked-jwt-token");

        String token = sessionService.login(new LoginRequest("alice", "password123"));

        assertThat(token).isEqualTo("mocked-jwt-token");
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        when(userService.findUserByUsername("ghost")).thenReturn(Optional.empty());

        // findUserByUsername returns Optional — orElseThrow fires when empty
        assertThatThrownBy(() -> sessionService.login(new LoginRequest("ghost", "pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        User user = new User();
        user.setUsername("alice");
        user.setHash("hashed");

        when(userService.findUserByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> sessionService.login(new LoginRequest("alice", "wrongpass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }
}

package com.backend.backend.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.backend.security.jwt.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtConfigTest {

    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();

        // JwtConfig has a private field "secretKey" that is normally filled by Spring
        // via @Value("${jwt.secret}") from application.properties.
        // But here we have no Spring running, so the field would stay null and crash.
        // ReflectionTestUtils.setField() lets us sneak past the "private" keyword
        // and set the field manually — like Spring would, but without Spring.
        ReflectionTestUtils.setField(jwtConfig, "secretKey", "test-secret-key-for-unit-tests");
    }

    @Test
    void createJWT_validUsername_returnsNonEmptyToken() {
        String token = jwtConfig.createJWT("alice");

        assertThat(token).isNotBlank();
    }

    @Test
    void verifyJWT_tokenCreatedByUs_returnsCorrectSubject() {
        String token = jwtConfig.createJWT("alice");

        DecodedJWT decoded = jwtConfig.verifyJWT(token);

        assertThat(decoded.getSubject()).isEqualTo("alice");
    }

    @Test
    void verifyJWT_tamperedToken_throwsRuntimeException() {
        assertThatThrownBy(() -> jwtConfig.verifyJWT("this.is.not.a.valid.token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid JWT");
    }
}

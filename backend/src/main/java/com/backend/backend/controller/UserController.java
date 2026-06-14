package com.backend.backend.controller;

import com.backend.backend.model.dto.RegisterRequest;
import com.backend.backend.model.dto.TokenResponse;
import com.backend.backend.model.entity.User;
import com.backend.backend.security.jwt.JwtConfig;
import com.backend.backend.service.declaration.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final IUserService userService;
    private final JwtConfig jwtConfig;

    public UserController(IUserService userService, JwtConfig jwtConfig) {
        this.userService = userService;
        this.jwtConfig = jwtConfig;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registration attempt for username '{}'", request.getUsername());
        User user = userService.registerUser(request);
        String token = jwtConfig.createJWT(user.getUsername());
        log.info("User '{}' registered successfully", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(token));
    }
}

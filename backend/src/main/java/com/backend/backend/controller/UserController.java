package com.backend.backend.controller;

import com.backend.backend.model.dto.RegisterRequest;
import com.backend.backend.model.dto.TokenResponse;
import com.backend.backend.model.dto.UpdateProfileRequest;
import com.backend.backend.model.entity.User;
import com.backend.backend.security.jwt.JwtConfig;
import com.backend.backend.service.declaration.IUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for username '{}'", request.getUsername());
        User user = userService.registerUser(request);
        String token = jwtConfig.createJWT(user.getUsername());
        log.info("User '{}' registered successfully", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(token));
    }

    // Username may change, so the old JWT's `sub` claim could be stale after this call —
    // we mint a fresh token and let the client swap it in.
    @PutMapping("/me")
    public ResponseEntity<TokenResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Profile update attempt by '{}'", currentUsername);
        User updated = userService.updateUser(currentUsername, request);
        String token = jwtConfig.createJWT(updated.getUsername());
        log.info("User id={} profile updated (username now '{}')", updated.getId(), updated.getUsername());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}

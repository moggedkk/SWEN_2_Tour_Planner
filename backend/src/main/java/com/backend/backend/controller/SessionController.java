package com.backend.backend.controller;

import com.backend.backend.model.dto.LoginRequest;
import com.backend.backend.model.dto.TokenResponse;
import com.backend.backend.service.declaration.ISessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@CrossOrigin
public class SessionController {

    private final ISessionService sessionService;

    public SessionController(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user '{}'", request.getUsername());
        String token = sessionService.login(request);
        log.info("Login successful for user '{}'", request.getUsername());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}

package com.backend.backend.controller;

import com.backend.backend.model.dto.LoginRequest;
import com.backend.backend.model.dto.TokenResponse;
import com.backend.backend.service.declaration.ISessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            String token = sessionService.login(request);
            return ResponseEntity.ok(new TokenResponse(token));         
    }
}

package com.backend.backend.controller;

import com.backend.backend.model.LoginRequest;
import com.backend.backend.service.declaration.ISessionService;
import com.backend.backend.service.declaration.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final ISessionService sessionService;
    @Autowired
    public SessionController(ISessionService sessionService) {
        this.sessionService = sessionService;
    }
    @CrossOrigin
    @PostMapping
    public void login(@RequestBody LoginRequest request) {
        this.sessionService.login(request);
    }
}

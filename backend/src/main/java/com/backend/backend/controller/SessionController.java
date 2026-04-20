package com.backend.backend.controller;

import com.backend.backend.model.LoginRequest;
import com.backend.backend.service.declaration.IUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final IUserService userService;
    public  SessionController(IUserService userService){
        this.userService = userService;
    }
    @CrossOrigin
    @PostMapping
    public void login(@RequestBody LoginRequest request){
        this.userService.loginUser(request);
    }
}

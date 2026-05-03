package com.backend.backend.controller;

import com.backend.backend.model.entity.User;
import com.backend.backend.service.declaration.IUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    private final IUserService userService;
    public  UserController(IUserService userService){
        this.userService = userService;
    }

    @PostMapping
    public String register(@RequestBody User user) {
        this.userService.registerUser(user);
        return "jwt";
    }
    @GetMapping
    public String hello() {
        return "Hello";
    }

    @GetMapping("/test")
    public String test() {
        return "Backend is accessible!";
    }
}
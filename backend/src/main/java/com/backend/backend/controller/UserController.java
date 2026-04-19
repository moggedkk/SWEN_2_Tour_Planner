package com.backend.backend.controller;

import com.backend.backend.model.User;
import com.backend.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    UserService userService;
    public  UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public User register(@RequestBody User user) {
        this.userService.RegisterUser(user);
        return user;
    }
}
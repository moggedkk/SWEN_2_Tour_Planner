package com.backend.backend.controller;

import com.backend.backend.model.LoginRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    @CrossOrigin
    @PostMapping("/sessions")
    public String login(@RequestBody LoginRequest request){
        return "example_token";
    }
}

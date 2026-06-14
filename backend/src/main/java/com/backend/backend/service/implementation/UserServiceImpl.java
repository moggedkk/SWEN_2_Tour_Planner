package com.backend.backend.service.implementation;

import com.backend.backend.exception.UsernameAlreadyExistsException;
import com.backend.backend.model.dto.RegisterRequest;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.UserRepository;
import com.backend.backend.service.declaration.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username '{}' already taken", request.getUsername());
            throw new UsernameAlreadyExistsException(request.getUsername());
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setHash(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        log.info("User '{}' registered with id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}

package com.backend.backend.service;

import com.backend.backend.exception.UsernameAlreadyExistsException;
import com.backend.backend.model.dto.RegisterRequest;
import com.backend.backend.model.dto.UpdateProfileRequest;
import com.backend.backend.model.entity.User;
import com.backend.backend.repository.UserRepository;
import com.backend.backend.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_newUsername_savesAndReturnsUser() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password123");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        User saved = new User();
        saved.setUsername("alice");
        saved.setEmail("alice@example.com");
        saved.setHash("hashed");
        when(userRepository.save(any())).thenReturn(saved);

        User result = userService.registerUser(request);

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getHash()).isEqualTo("hashed");
    }

    @Test
    void registerUser_duplicateUsername_throwsUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password123");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("alice");
    }

    @Test
    void findUserByUsername_existingUser_returnsOptionalWithUser() {
        User user = new User();
        user.setUsername("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void findUserByUsername_unknownUser_returnsEmptyOptional() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserByUsername("nobody");

        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_validRequest_updatesAndReturnsUser() {
        // alice keeps her name but changes email + password — uniqueness check should
        // be skipped entirely since the username didn't change.
        User existing = new User();
        existing.setUsername("alice");
        existing.setEmail("old@example.com");
        existing.setHash("oldhash");

        UpdateProfileRequest request = new UpdateProfileRequest("alice", "new@example.com", "newpassword");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpassword")).thenReturn("newhash");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        User result = userService.updateUser("alice", request);

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getHash()).isEqualTo("newhash");
        // no rename -> shouldn't have asked the repo if "alice" already exists
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void updateUser_newUsernameAlreadyTaken_throwsUsernameAlreadyExists() {
        // alice tries to rename herself to "bob" but bob already exists.
        User existing = new User();
        existing.setUsername("alice");

        UpdateProfileRequest request = new UpdateProfileRequest("bob", "alice@example.com", "password123");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser("alice", request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("bob");

        verify(userRepository, never()).save(any(User.class));
    }
}

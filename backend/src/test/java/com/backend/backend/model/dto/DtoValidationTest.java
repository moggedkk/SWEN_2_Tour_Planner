package com.backend.backend.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Runs jakarta.validation against the DTOs directly — no Spring context, no controllers.
// If these pass, the same constraints fire on @Valid @RequestBody in the controllers
// (Spring uses the same Validator under the hood). Cheap, fast, no app startup needed.
class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    // ---- TourRequest ----

    @Test
    void tourRequest_validPayload_passes() {
        TourRequest r = new TourRequest("Sunday Bike", "Vienna", "Graz",
                "A relaxed Sunday cycling tour from Vienna to Graz.",
                "Moderate", "cycling-regular");

        assertThat(validator.validate(r)).isEmpty();
    }

    @Test
    void tourRequest_blankName_failsWithFieldError() {
        TourRequest r = new TourRequest("", "Vienna", "Graz",
                "A long enough description for validation.",
                "Easy", "foot-walking");

        Set<ConstraintViolation<TourRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("name");
    }

    @Test
    void tourRequest_shortDescription_failsSizeConstraint() {
        TourRequest r = new TourRequest("Valid Name", "Vienna", "Graz",
                "too short", // < 10 chars
                "Easy", "foot-walking");

        Set<ConstraintViolation<TourRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("description");
    }

    // ---- TourLogRequest ----

    @Test
    void tourLogRequest_validPayload_passes() {
        TourLogRequest r = new TourLogRequest(
                LocalDateTime.now().minusDays(1),
                "Great ride", "Easy",
                12.5, 90.0, 4,
                null, null);

        assertThat(validator.validate(r)).isEmpty();
    }

    @Test
    void tourLogRequest_futureDate_failsPastOrPresent() {
        TourLogRequest r = new TourLogRequest(
                LocalDateTime.now().plusDays(7),
                "Future log?", "Easy",
                12.5, 90.0, 4,
                null, null);

        Set<ConstraintViolation<TourLogRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("dateTime");
    }

    @Test
    void tourLogRequest_zeroDuration_failsMin() {
        TourLogRequest r = new TourLogRequest(
                LocalDateTime.now(),
                "ok", "Easy",
                10.0, 0.0, 3,        // totalTime = 0 should fail @Min(1)
                null, null);

        Set<ConstraintViolation<TourLogRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("totalTime");
    }

    @Test
    void tourLogRequest_ratingOutOfRange_failsMax() {
        TourLogRequest r = new TourLogRequest(
                LocalDateTime.now(),
                "ok", "Easy",
                10.0, 30.0, 99,      // rating > 5
                null, null);

        Set<ConstraintViolation<TourLogRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("rating");
    }

    // ---- RegisterRequest ----

    @Test
    void registerRequest_validPayload_passes() {
        RegisterRequest r = new RegisterRequest("alice", "alice@example.com", "supersecret");
        assertThat(validator.validate(r)).isEmpty();
    }

    @Test
    void registerRequest_invalidEmail_fails() {
        RegisterRequest r = new RegisterRequest("alice", "not-an-email", "supersecret");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("email");
    }

    @Test
    void registerRequest_shortPassword_failsSize() {
        RegisterRequest r = new RegisterRequest("alice", "alice@example.com", "x");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("password");
    }

    // ---- LoginRequest ----

    @Test
    void loginRequest_blankCredentials_failBothFields() {
        LoginRequest r = new LoginRequest("", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(r);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("username", "password");
    }
}

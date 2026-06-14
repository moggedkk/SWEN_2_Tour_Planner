package com.backend.backend.exception;

import com.backend.backend.model.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    // No Spring, no mocks needed — we just call the handler methods directly.
    // In the real app Spring calls these automatically when an exception is thrown,
    // but here we skip that and call them like normal Java methods to check the response.
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUsernameAlreadyExists_returnsConflict() {
        UsernameAlreadyExistsException ex = new UsernameAlreadyExistsException("alice");

        ResponseEntity<ErrorResponse> response = handler.handleUsernameAlreadyExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("alice");
    }

    @Test
    void handleInvalidCredentials_returnsUnauthorized() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isNotBlank();
    }

    @Test
    void handleTourNotFound_returnsNotFound() {
        TourNotFoundException ex = new TourNotFoundException(42);

        ResponseEntity<ErrorResponse> response = handler.handleTourNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("42");
    }

    @Test
    void handleRouteNotFound_returnsUnprocessableEntity() {
        RouteNotFoundException ex = new RouteNotFoundException("Vienna", "Moon");

        ResponseEntity<ErrorResponse> response = handler.handleRouteNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getMessage()).contains("Vienna");
    }
}

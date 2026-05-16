package com.backend.backend.exception;

public class TourNotFoundException extends RuntimeException {
    public TourNotFoundException(int id) {
        super("Tour not found: " + id);
    }
}

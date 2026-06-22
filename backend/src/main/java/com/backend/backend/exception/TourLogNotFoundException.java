package com.backend.backend.exception;

public class TourLogNotFoundException extends RuntimeException {
    public TourLogNotFoundException(int id) {
        super("Tour log not found: " + id);
    }
}

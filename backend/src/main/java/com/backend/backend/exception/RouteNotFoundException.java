package com.backend.backend.exception;

public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(String from, String to) {
        super("No route found between \"" + from + "\" and \"" + to + "\". Make sure both locations are reachable by the selected transport type.");
    }

    public RouteNotFoundException(String message) {
        super(message);
    }
}

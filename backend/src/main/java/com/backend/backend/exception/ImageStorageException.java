package com.backend.backend.exception;

// Thrown when something is wrong with an uploaded image — e.g. the filename
// tries to escape the storage dir, the extension isn't on the allowlist, or
// the base64 payload is malformed. We turn this into a 400 in the handler
// because it's always the client's fault, not ours.
public class ImageStorageException extends RuntimeException {
    public ImageStorageException(String message) {
        super(message);
    }

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

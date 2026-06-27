package com.backend.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Serves the images saved by TourLogServiceImpl.saveImage().
// Auth is required — the path includes the username, and we only let users
// fetch files inside their OWN folder. Anything else gets a 404 so we don't
// leak whether a path exists for somebody else.
@RestController
@RequestMapping("/api/images")
@CrossOrigin
public class ImageController {
    @Value("${storage.image-dir}")
    private String baseImagePath;

    @GetMapping("/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request, Authentication authentication) throws IOException {
        // SecurityConfig requires auth on this endpoint, so principal should never be null —
        // but check anyway so a misconfigured filter chain can't silently expose files.
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();

        // Pull "<user>/<tourId>/<file>" out of the URL and decode any %-escaped chars
        // (the frontend uses encodeURI, so e.g. spaces in filenames arrive as %20).
        String requested = request.getRequestURI().substring("/api/images/".length());
        requested = URLDecoder.decode(requested, StandardCharsets.UTF_8);

        // Resolve + normalize, then make sure the final path stays under baseDir AND
        // under the caller's own username dir. Both checks matter — the first stops
        // "../" traversal, the second stops one user fetching another user's images.
        Path baseDir = Paths.get(baseImagePath).toAbsolutePath().normalize();
        Path userDir = baseDir.resolve(username).normalize();
        Path file = baseDir.resolve(requested).normalize();
        if (!file.startsWith(userDir)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(resource);
    }
}

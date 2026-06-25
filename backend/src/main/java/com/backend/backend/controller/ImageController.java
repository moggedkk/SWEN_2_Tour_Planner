package com.backend.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
@CrossOrigin
public class ImageController {
    @Value("${storage.image-dir}")
    private String baseImagePath;

    @GetMapping("/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) throws IOException {
        String fullPath = request.getRequestURI()
                .replace("/api/images/", "");
        Path file = Paths.get(baseImagePath, fullPath);

        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(file);
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                contentType != null
                                        ? contentType
                                        : MediaType.APPLICATION_OCTET_STREAM_VALUE
                        )
                )
                .body(resource);
    }
}

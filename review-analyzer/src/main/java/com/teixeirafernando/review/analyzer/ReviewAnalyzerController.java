package com.teixeirafernando.review.analyzer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ReviewAnalyzerController {

    private final ReviewAnalyzerStorageService storageService;
    private final ApplicationProperties properties;

    public ReviewAnalyzerController(
            ReviewAnalyzerStorageService storageService,
            ApplicationProperties properties
    ) {
        this.storageService = storageService;
        this.properties = properties;
    }

    @GetMapping(value = "/api/messages/{id}",  produces = { "application/json" })
    public ResponseEntity<String> get(@PathVariable String id) throws IOException {
        if (storageService.reviewExists(properties.bucket(), id)){
            return ResponseEntity.ok(storageService.read(properties.bucket(), id).toString());
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Analyzed Review with the provided id was not found. Please check if the id is correct.");
        }

    }
}

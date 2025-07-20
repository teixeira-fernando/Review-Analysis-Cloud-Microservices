package com.teixeirafernando.review.collector.integration;

import com.teixeirafernando.review.collector.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest
public class ReviewCollectorIntegrationTest extends TestContainersConfiguration{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    ReviewCollectorService reviewCollectorService;

    @Test
    @DisplayName("POST /api/review - Success")
    void testSuccessfulNewReviewCreation() throws Exception {

        // Arrange
        Review review = new Review(
                "Product Name",
                "Customer Name",
                "This is a review content",
                5.0
        );

        // Act & Assert
        mockMvc.perform(post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Objects.requireNonNull(review.toString())))
                .andExpect(status().isOk()) //validate 200 response code
                .andExpect(jsonPath("$.id").value(review.getId().toString()));//validate response body

    }
}

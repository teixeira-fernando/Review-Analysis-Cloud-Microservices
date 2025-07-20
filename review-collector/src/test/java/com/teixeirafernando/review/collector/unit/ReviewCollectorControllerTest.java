package com.teixeirafernando.review.collector.unit;


import com.teixeirafernando.review.collector.Review;
import com.teixeirafernando.review.collector.ReviewCollectorController;
import com.teixeirafernando.review.collector.ReviewCollectorService;
import com.teixeirafernando.review.collector.ApplicationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewCollectorController.class)
class ReviewCollectorControllerTest {

    @MockBean
    private ReviewCollectorService reviewCollectorService;

    @MockBean
    private ApplicationProperties properties;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSuccessfulNewReview() throws Exception {
        // Arrange
        Review review = new Review(
                "Test Product",
                "Customer Name",
                "This is a review content",
                5.0
        );
        String queueName = "test-queue";

        // Mock the behavior of ApplicationProperties and ReviewCollectorService
        when(properties.queue()).thenReturn(queueName);

        // Act & Assert
        mockMvc.perform(post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(review)))
                .andExpect(status().isOk()) //validate 200 response code
                .andExpect(jsonPath("$.id").value(review.getId().toString()))
                .andExpect(jsonPath("$.productName").value(review.getProductName()))
                .andExpect(jsonPath("$.customerName").value(review.getCustomerName()))
                .andExpect(jsonPath("$.reviewContent").value(review.getReviewContent()))
                .andExpect(jsonPath("$.rating").value(review.getRating()));
    }

    @Test
    void testCorrectResponseForInvalidReview() throws Exception {
        // Arrange
        String queueName = "test-queue";

        // Mock the behavior of ApplicationProperties and ReviewCollectorService
        when(properties.queue()).thenReturn(queueName);

        // Act & Assert
        mockMvc.perform(post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + UUID.randomUUID() + "}"))
                .andExpect(status().isBadRequest()); //validate 400 response code
    }
}

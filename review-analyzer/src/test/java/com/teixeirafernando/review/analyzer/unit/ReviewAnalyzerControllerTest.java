package com.teixeirafernando.review.analyzer.unit;

import com.teixeirafernando.review.analyzer.AnalyzedReview;
import com.teixeirafernando.review.analyzer.ApplicationProperties;
import com.teixeirafernando.review.analyzer.ReviewAnalyzerController;
import com.teixeirafernando.review.analyzer.ReviewAnalyzerStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewAnalyzerController.class)
public class ReviewAnalyzerControllerTest {

    @MockBean
    private ReviewAnalyzerStorageService reviewCollectorService;

    @MockBean
    private ApplicationProperties properties;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSuccessfulGETAnalyzedReview() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        AnalyzedReview review = new AnalyzedReview(id, UUID.randomUUID(),"Customer Name", "that is the content of my review", 5.0);

        String bucketName = "test-bucket";

        // Mock the behavior of ApplicationProperties and ReviewCollectorService
        when(properties.bucket()).thenReturn(bucketName);
        when(reviewCollectorService.reviewExists(properties.bucket(), id.toString())).thenReturn(true);
        when(reviewCollectorService.read(properties.bucket(), id.toString())).thenReturn(review);

        // Act & Assert
        mockMvc.perform(get("/api/messages/"+id))
                .andExpect(status().isOk()) //validate 200 response code
                .andExpect(jsonPath("$.id").value(review.getId().toString()))
                .andExpect(jsonPath("$.productId").value(review.getProductId().toString()))
                .andExpect(jsonPath("$.customerName").value(review.getCustomerName()))
                .andExpect(jsonPath("$.reviewContent").value(review.getReviewContent()))
                .andExpect(jsonPath("$.rating").value(review.getRating()))
                .andExpect(jsonPath("$.reviewAnalysis").value(review.doReviewAnalysis()));
    }

    @Test
    void testNotFoundAnalyzedReview() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        String bucketName = "test-bucket";

        // Mock the behavior of ApplicationProperties and ReviewCollectorService
        when(properties.bucket()).thenReturn(bucketName);
        when(reviewCollectorService.reviewExists(properties.bucket(), id.toString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/messages/"+id))
                .andExpect(status().isNotFound());
    }
}

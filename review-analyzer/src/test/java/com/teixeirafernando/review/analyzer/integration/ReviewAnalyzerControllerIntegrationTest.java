package com.teixeirafernando.review.analyzer.integration;

import com.teixeirafernando.review.analyzer.AnalyzedReview;
import com.teixeirafernando.review.analyzer.ApplicationProperties;
import com.teixeirafernando.review.analyzer.ReviewAnalyzerStorageService;
import com.teixeirafernando.review.analyzer.TestContainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest
public class ReviewAnalyzerControllerIntegrationTest extends TestContainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ReviewAnalyzerStorageService reviewAnalyzerStorageService;

    @Autowired
    ApplicationProperties properties;

    @Test
    @DisplayName("GET /api/messages/{id} - Success")
    void shouldReturnReviewsPresentInS3Bucket() throws Exception {

        //Arrange
        UUID id = UUID.randomUUID();
        AnalyzedReview analyzedReview = new AnalyzedReview(id,UUID.randomUUID(),"test","test",5.0);

        this.reviewAnalyzerStorageService.store(TestContainersConfiguration.BUCKET_NAME, id.toString(), analyzedReview);

        // Act & Assert
        mockMvc.perform(get("/api/messages/"+id))
                .andExpect(status().isOk()) //validate 200 response code
                .andExpect(jsonPath("$.id").value(id.toString()));//validate response body
    }
}

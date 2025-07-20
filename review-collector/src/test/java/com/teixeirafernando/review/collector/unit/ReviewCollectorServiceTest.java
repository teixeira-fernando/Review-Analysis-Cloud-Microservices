package com.teixeirafernando.review.collector.unit;

import com.teixeirafernando.review.collector.Review;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import com.teixeirafernando.review.collector.ReviewCollectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReviewCollectorServiceTest {

    @Mock
    private SqsTemplate sqsTemplate;

    private ReviewCollectorService reviewCollectorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewCollectorService = new ReviewCollectorService(sqsTemplate);
    }

    @Test
    void testSendAsyncIsCalledWithCorrectArguments() {
        // Arrange
        String queueName = "test-queue";
        Review review = new Review("Product Name","Customer Name", "that is the content of my review", 5.0);

        // Act
        reviewCollectorService.publish(queueName, review.toString());

        // Assert
        // Capture the arguments passed to sendAsync
        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> reviewCaptor = ArgumentCaptor.forClass(String.class);

        verify(sqsTemplate, times(1)).send(queueNameCaptor.capture(), reviewCaptor.capture());

        // Assert the captured values
        assertThat(queueName).isEqualTo(queueNameCaptor.getValue(), "Queue name should match");
        assertThat(review.toString()).isEqualTo(reviewCaptor.getValue().toString(), "Review should match");
    }
}
package com.teixeirafernando.review.analyzer.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teixeirafernando.review.analyzer.*;
import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReviewAnalyzerMessageListenerServiceTest
{

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private S3Template s3Template;

    @Mock
    private ApplicationProperties properties;

    private ReviewAnalyzerStorageService reviewAnalyzerStorageService;

    private ReviewAnalyzerMessageListenerService reviewAnalyzerMessageListenerService;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewAnalyzerStorageService = new ReviewAnalyzerStorageService(s3Template);
        reviewAnalyzerMessageListenerService = new ReviewAnalyzerMessageListenerService(sqsTemplate, reviewAnalyzerStorageService, properties);
    }

    @Test
    void testSendAsyncIsCalledWithCorrectArguments() throws IOException, MessageProcessingException {
        // Arrange
        UUID id = UUID.randomUUID();
        String bucketName = this.properties.bucket();
        AnalyzedReview review = new AnalyzedReview(id, UUID.randomUUID(),"Customer Name", "that is the content of my review", 5.0);

        // Create a message
        Message message = Message.builder().body(review.toString()).build();

        // Act
        reviewAnalyzerMessageListenerService.handle(message);

        // Assert
        // Capture the arguments passed to upload
        ArgumentCaptor<String> bucketNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AnalyzedReview> objectCaptor = ArgumentCaptor.forClass(AnalyzedReview.class);

        verify(s3Template, times(1)).store(bucketNameCaptor.capture(), keyCaptor.capture(), objectCaptor.capture());

        // Assert the captured values
        assertThat(id.toString()).isEqualTo(keyCaptor.getValue(), "Object key should match");
        assertThat(review.toString()).isEqualTo(objectCaptor.getValue().toString(), "Analyzed Review should match");
    }
}

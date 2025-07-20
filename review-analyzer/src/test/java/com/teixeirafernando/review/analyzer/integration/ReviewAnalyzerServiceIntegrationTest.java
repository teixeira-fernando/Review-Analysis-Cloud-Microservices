package com.teixeirafernando.review.analyzer.integration;

import com.fasterxml.jackson.core.JsonParseException;
import com.teixeirafernando.review.analyzer.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ReviewAnalyzerServiceIntegrationTest extends TestContainersConfiguration {

    @Autowired
    ReviewAnalyzerStorageService reviewAnalyzerStorageService;

    @SpyBean
    ReviewAnalyzerMessageListenerService reviewAnalyzerMessageListenerService;

    @Autowired
    ApplicationProperties properties;

    @Test
    void shouldProcessMessagesInTheQueueSuccessfullyAndPushToS3Bucket() throws IOException, InterruptedException, JSONException {
        this.insertTestDataToSQSQueue("""
                {
                    "id": "e921412f-af4b-4b1f-bec4-734982b2fb9c",
                    "productName": "Test Product",
                    "customerName": "test",
                    "reviewContent": "test",
                    "rating": 5.0
                }
                """);

        await()
                .pollInterval(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    verify(reviewAnalyzerMessageListenerService).handle(any(Message.class));
                        });


        boolean bucketExists = this.reviewAnalyzerStorageService.bucketExists(TestContainersConfiguration.BUCKET_NAME);
        boolean reviewExists = this.reviewAnalyzerStorageService.reviewExists(TestContainersConfiguration.BUCKET_NAME, "e921412f-af4b-4b1f-bec4-734982b2fb9c");
        JSONObject review = new JSONObject(this.reviewAnalyzerStorageService.downloadAsString(TestContainersConfiguration.BUCKET_NAME, "e921412f-af4b-4b1f-bec4-734982b2fb9c"));

        assertThat(bucketExists).isTrue();
        assertThat(reviewExists).isTrue();
        assertThat(review.getString("id")).isEqualTo("e921412f-af4b-4b1f-bec4-734982b2fb9c");
        assertThat(review.getString("reviewAnalysis")).isNotNull();

        System.out.println("Value of queue-name:"+TestContainersConfiguration.QUEUE_NAME);

    }

    @ParameterizedTest
    @MethodSource("provideMessagesForTest")
    void shouldRejectMessagesWithMissingFieldsOrIncorrectFormat(String id, String message) throws IOException, InterruptedException, JSONException {
        this.insertTestDataToSQSQueue(message);

        await()
                .pollInterval(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThrows(MessageProcessingException.class, () -> reviewAnalyzerMessageListenerService.handle(any(Message.class)));
                });

        boolean bucketExists = this.reviewAnalyzerStorageService.bucketExists(TestContainersConfiguration.BUCKET_NAME);
        boolean reviewExists = this.reviewAnalyzerStorageService.reviewExists(TestContainersConfiguration.BUCKET_NAME, id);

        assertThat(bucketExists).isTrue();
        assertThat(reviewExists).isFalse();
    }

    private static Stream<Arguments> provideMessagesForTest() {
        return Stream.of(
                Arguments.of("b9f2d265-fe7e-48e9-bf6d-f250502cd068", """
                {
                    "id": "b9f2d265-fe7e-48e9-bf6d-f250502cd068",
                }
                """)
        );
    }
}

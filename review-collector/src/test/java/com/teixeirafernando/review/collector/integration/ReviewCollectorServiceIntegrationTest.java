package com.teixeirafernando.review.collector.integration;

import com.teixeirafernando.review.collector.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.sqs.model.Message;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		classes = ReviewCollectorApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ReviewCollectorServiceIntegrationTest extends TestContainersConfiguration {

	@Autowired
	ReviewCollectorService reviewCollectorService;

	@Autowired
	ApplicationProperties properties;

	@Test
	void shouldHandleMessageSuccessfully() {
		Review review = new Review("Product Name", "Customer Name", "that is the content of my review", 5.0);
		reviewCollectorService.publish(properties.queue(), review.toString());

		String queueUrl = getQueueUrl();

		await()
				.pollInterval(Duration.ofSeconds(2))
				.atMost(Duration.ofSeconds(10))
				.ignoreExceptions()
				.untilAsserted(() -> {
					List<Message> messages = sqsClient.receiveMessage(b -> b
							.queueUrl(queueUrl)
							.maxNumberOfMessages(1)
					).messages();

					assertThat(messages).hasSize(1);
					assertThat(messages.get(0).body()).isEqualTo(review.toString());
				});
	}

}

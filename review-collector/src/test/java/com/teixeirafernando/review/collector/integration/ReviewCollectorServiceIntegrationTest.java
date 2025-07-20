package com.teixeirafernando.review.collector.integration;

import com.teixeirafernando.review.collector.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.UUID;

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

		await()
				.pollInterval(Duration.ofSeconds(2))
				.atMost(Duration.ofSeconds(10))
				.ignoreExceptions()
				.untilAsserted(() -> {
					JSONObject messageFromSQS = new JSONObject(localStack.execInContainer(
							"awslocal",
							"sqs",
							"receive-message",
							"--queue-url",
							SQSUrl+"/"+QUEUE_NAME
					).getStdout());

					assertThat(messageFromSQS.getJSONArray("Messages").length()).isEqualTo(1);
					assertThat(messageFromSQS.getJSONArray("Messages").getJSONObject(0).get("Body")).isEqualTo(review.toString());
				});
	}

}

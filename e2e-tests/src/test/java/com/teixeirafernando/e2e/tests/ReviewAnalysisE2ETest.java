package com.teixeirafernando.e2e.tests;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class ReviewAnalysisE2ETest extends TestContainersConfiguration {

    @Test
    @DisplayName("Create a new Review and make the sentiment analysis")
    void analyzeReviewSuccessfully(){

        String review = """
                {
                    "productId": "da6037a6-a375-40e2-a8a6-1bb5f9448df0",
                    "customerName": "test",
                    "reviewContent": "test",
                    "rating": 5.0
                }
                """;

        String reviewCollectorURL = "http://localhost:8080";
        String reviewAnalyzerURL = "http://localhost:8081";

        System.out.println(review);

        String id = given()
                .contentType("application/json")
                .body(review)
                .when()
                .post(reviewCollectorURL + "/api/review")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("productId", equalTo("da6037a6-a375-40e2-a8a6-1bb5f9448df0"))
                .extract()
                .path("id");


        await()
                .pollInterval(Duration.ofSeconds(5))
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    given()
                            .contentType("application/json")
                            .when()
                            .get(reviewAnalyzerURL + "/api/messages/"+id)
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_OK)
                            .body("id", equalTo(id))
                            .body("reviewAnalysis", notNullValue());
                });

    }
}

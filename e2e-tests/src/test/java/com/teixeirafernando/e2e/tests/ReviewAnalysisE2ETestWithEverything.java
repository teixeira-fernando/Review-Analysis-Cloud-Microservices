package com.teixeirafernando.e2e.tests;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReviewAnalysisE2ETestWithEverything extends TestContainersConfigurationWithEverything {

    @Test
    @DisplayName("Create a new Review and make the sentiment analysis")
    void analyzeReviewSuccessfully(){

        String review = """
                {
                    "productName": "Test Product",
                    "customerName": "test",
                    "reviewContent": "test",
                    "rating": 5.0
                }
                """;

        String fullReviewCollectorURL = "http://"+ TestContainersConfigurationWithEverything.ReviewCollectorService.getHost()+":8080";
        String fullReviewAnalyzerURL = "http://"+ TestContainersConfigurationWithEverything.ReviewAnalyzerService.getHost()+":8081";

        String id = given()
                .contentType("application/json")
                .body(review)
                .when()
                .post(fullReviewCollectorURL + "/api/review")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("productName", equalTo("Test Product"))
                .extract()
                .path("id");

        System.out.println("First request worked as expected");


        await()
                .pollInterval(Duration.ofSeconds(5))
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    given()
                            .contentType("application/json")
                            .when()
                            .get(fullReviewAnalyzerURL + "/api/messages/" + id)
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_OK)
                            .body("id", equalTo(id))
                            .body("reviewAnalysis", notNullValue());
                });

    }
}

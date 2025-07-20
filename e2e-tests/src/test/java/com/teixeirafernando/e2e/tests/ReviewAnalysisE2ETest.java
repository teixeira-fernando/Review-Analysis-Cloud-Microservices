package com.teixeirafernando.e2e.tests;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

public class ReviewAnalysisE2ETest extends TestContainersConfiguration {

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

        String baseReviewCollectorURL = Optional.ofNullable(System.getenv("REVIEW_COLLECTOR_BASE_URL")).orElse("localhost");
        String baseReviewAnalyzerURL = Optional.ofNullable(System.getenv("REVIEW_ANALYZER_BASE_URL")).orElse("localhost");

        String fullReviewCollectorURL = "http://" + baseReviewCollectorURL + ":8080";
        String fullReviewAnalyzerURL = "http://" + baseReviewAnalyzerURL + ":8081";

        System.out.println(fullReviewCollectorURL);
        System.out.println(fullReviewAnalyzerURL);
        System.out.println(review);

        given().when().get(fullReviewCollectorURL + "/actuator/health").then().statusCode(HttpStatus.SC_OK).body(equalTo("{\"status\":\"UP\"}"));

        System.out.println("review collector is up and running");

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

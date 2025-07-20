package com.teixeirafernando.review.analyzer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class AnalyzedReview {

    @Getter @Setter private UUID id;
    @Getter @Setter private String productName;
    @Getter @Setter private String customerName;
    @Getter @Setter private String reviewContent;
    @Getter @Setter private double rating;
    @Getter @Setter private String reviewAnalysis;

    @JsonCreator
    public AnalyzedReview(
            @JsonProperty("id") UUID id,
            @JsonProperty("productName") String productName,
            @JsonProperty("customerName") String customerName,
            @JsonProperty("reviewContent") String reviewContent,
            @JsonProperty("rating") double rating){
        this.id = id;
        this.productName = productName;
        this.customerName = customerName;
        this.reviewContent = reviewContent;
        this.rating = rating;
        this.doReviewAnalysis();
    }

    // Convert to JSON String
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String doReviewAnalysis(){
        // TODO: Call an external API for an AI analysis or something similar to have better data for this field
        this.reviewAnalysis = "That's the analysis of the review, checking if it is something positive or negative and what kind of emotions does the customer had during the review";
        return this.reviewAnalysis;
    }

}

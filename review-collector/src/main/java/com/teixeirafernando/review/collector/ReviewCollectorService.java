package com.teixeirafernando.review.collector;

import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class ReviewCollectorService {

    private final SqsTemplate sqsTemplate;

    public ReviewCollectorService(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    public SendResult<String> publish(String queueName, String jsonReview) {
        try {
            return sqsTemplate.send(queueName, jsonReview);
        }
        catch (Exception ex){
            System.out.println("Error publishing message to queue: " + ex.getMessage());
            throw ex;
        }
    }
}

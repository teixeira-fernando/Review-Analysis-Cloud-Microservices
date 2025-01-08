package com.teixeirafernando.review.analyzer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class ReviewAnalyzerMessageListenerService {

    private final SqsTemplate sqsTemplate;
    private final ReviewAnalyzerStorageService storageService;
    private final ApplicationProperties properties;

    public ReviewAnalyzerMessageListenerService(
            SqsTemplate sqsTemplate,
            ReviewAnalyzerStorageService storageService,
            ApplicationProperties properties
    ) {
        this.sqsTemplate = sqsTemplate;
        this.storageService = storageService;
        this.properties = properties;
    }

    @SqsListener(queueNames = { "${app.queue}" })
    public void handle(Message sqsMessage) throws JsonProcessingException, MessageProcessingException {
        String bucketName = this.properties.bucket();
        ObjectMapper mapper = new ObjectMapper();

        AnalyzedReview analyzedReview;

        // De-serialize to an object
        try {
            analyzedReview = mapper.readValue(sqsMessage.body(), AnalyzedReview.class);
        }
        catch (Exception ex){
            throw new MessageProcessingException("Failure to process the Message in SQS queue. The reason could be that the message format is not correct.");
        }

        System.out.println(analyzedReview.toString());
        ByteArrayInputStream is = new ByteArrayInputStream(
                analyzedReview.toString().getBytes(StandardCharsets.UTF_8)
        );

        String key = analyzedReview.getId().toString();
        this.storageService.store(bucketName, key, analyzedReview);

        System.out.println("Stored File "+key+"to bucket "+bucketName);
    }
}

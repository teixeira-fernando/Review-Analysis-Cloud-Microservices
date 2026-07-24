package com.teixeirafernando.e2e.tests;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.util.UUID;

@Testcontainers
public abstract class TestContainersConfiguration {

    @Container
    protected static FlociContainer floci = new FlociContainer();

    static protected final String BUCKET_NAME = UUID.randomUUID().toString();
    static protected final String QUEUE_NAME = UUID.randomUUID().toString();

    @BeforeAll
    static void beforeAll() {
        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(floci.getEndpoint()))
                .region(Region.of(floci.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
                .forcePathStyle(true)
                .build();

        SqsClient sqsClient = SqsClient.builder()
                .endpointOverride(URI.create(floci.getEndpoint()))
                .region(Region.of(floci.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
                .build();

        s3Client.createBucket(b -> b.bucket(BUCKET_NAME));
        sqsClient.createQueue(b -> b.queueName(QUEUE_NAME));
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.bucket", () -> BUCKET_NAME);
        registry.add("app.queue", () -> QUEUE_NAME);
        registry.add(
                "spring.cloud.aws.region.static",
                () -> floci.getRegion()
        );
        registry.add(
                "spring.cloud.aws.credentials.access-key",
                () -> floci.getAccessKey()
        );
        registry.add(
                "spring.cloud.aws.credentials.secret-key",
                () -> floci.getSecretKey()
        );
        registry.add(
                "spring.cloud.aws.s3.endpoint",
                () -> floci.getEndpoint()
        );
        registry.add(
                "spring.cloud.aws.sqs.endpoint",
                () -> floci.getEndpoint()
        );
    }
}


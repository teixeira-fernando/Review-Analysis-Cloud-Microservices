package com.teixeirafernando.e2e.tests;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.AbstractImagePullPolicy;
import org.testcontainers.images.ImageData;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
public class TestContainerConfigurationWithEverything {

    private static final Network SHARED_NETWORK = Network.newNetwork();
    protected static GenericContainer<?> ReviewCollectorService;
    protected static GenericContainer<?> ReviewAnalyzerService;

    @Container
    protected static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.0.3")
    ).withNetwork(SHARED_NETWORK).withNetworkAliases("localstack");



    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
        localStack.execInContainer(
                "awslocal",
                "sqs",
                "create-queue",
                "--queue-name",
                QUEUE_NAME
        );
        System.out.println("Before All block was executed");

        System.out.println("Initializer block was started");

        ReviewCollectorService = createReviewCollectorServiceContainer();
        ReviewAnalyzerService = createReviewAnalyzerServiceContainer();

        Startables.deepStart(ReviewCollectorService, ReviewAnalyzerService).join();
        //setPropertiesForConnections(environment);
        System.out.println("Initializer block was finished");

    }

    static protected final String BUCKET_NAME = "review-analysis-bucket";
    static protected final String QUEUE_NAME = "review-analysis-queue";

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.bucket", () -> BUCKET_NAME);
        registry.add("app.queue", () -> QUEUE_NAME);
        registry.add(
                "spring.cloud.aws.region.static",
                () -> localStack.getRegion()
        );
        registry.add(
                "spring.cloud.aws.credentials.access-key",
                () -> localStack.getAccessKey()
        );
        registry.add(
                "spring.cloud.aws.credentials.secret-key",
                () -> localStack.getSecretKey()
        );
        registry.add(
                "spring.cloud.aws.s3.endpoint",
                () -> localStack.getEndpointOverride(S3).toString()
        );
        registry.add(
                "spring.cloud.aws.sqs.endpoint",
                () -> localStack.getEndpointOverride(SQS).toString()
        );
    }


    private static GenericContainer<?> createReviewCollectorServiceContainer() {
        /*final var reviewCollectorImage = requireNonNull(
                System.getenv("image.review-collector"),
                "Review-collector image is null"
        );*/
        final var reviewCollectorImage = "teixeirafernando/review-collector:latest";
        return new GenericContainer<>(reviewCollectorImage)
                .withEnv("AWS_ENDPOINT", "http://localstack:4566")
                .withExposedPorts(8080)
                .withNetwork(SHARED_NETWORK)
                .withNetworkAliases("review-analysis")
                .withCreateContainerCmdModifier(
                        cmd -> cmd.withHostConfig(
                                new HostConfig()
                                        .withNetworkMode(SHARED_NETWORK.getId())
                                        .withPortBindings(new PortBinding(
                                                Ports.Binding.bindPort(8080),
                                                new ExposedPort(8080)
                                        ))
                        )
                )
                .waitingFor(
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                )
                .withImagePullPolicy(new AbstractImagePullPolicy() {
                    @Override
                    protected boolean shouldPullCached(DockerImageName imageName,
                                                       ImageData localImageData) {
                        return true;
                    }
                })
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Review-collector-service")));
    }

    private static GenericContainer<?> createReviewAnalyzerServiceContainer() {
        /*final var reviewAnalyzerImage = requireNonNull(
                System.getenv("image.review-analyzer"),
                "Review-analyzer image is null"
        );*/
        final var reviewAnalyzerImage = "teixeirafernando/review-analyzer:latest";
        return new GenericContainer<>(reviewAnalyzerImage)
                .withEnv("AWS_ENDPOINT", "http://localstack:4566")
                .withExposedPorts(8081)
                .withNetwork(SHARED_NETWORK)
                .withNetworkAliases("review-analysis")
                .withCreateContainerCmdModifier(
                        cmd -> cmd.withHostConfig(
                                new HostConfig()
                                        .withNetworkMode(SHARED_NETWORK.getId())
                                        .withPortBindings(new PortBinding(
                                                Ports.Binding.bindPort(8081),
                                                new ExposedPort(8081)
                                        ))
                        )
                )
                .waitingFor(
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                )
                .withImagePullPolicy(new AbstractImagePullPolicy() {
                    @Override
                    protected boolean shouldPullCached(DockerImageName imageName,
                                                       ImageData localImageData) {
                        return true;
                    }
                })
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Review-analyzer-service")));
    }

}


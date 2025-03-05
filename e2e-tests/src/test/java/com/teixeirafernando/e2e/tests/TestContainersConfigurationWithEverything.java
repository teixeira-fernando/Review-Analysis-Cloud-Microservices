package com.teixeirafernando.e2e.tests;

//other i
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Optional;

@Testcontainers
public class TestContainersConfigurationWithEverything {

    private static final Network SHARED_NETWORK = Network.newNetwork();
    protected static GenericContainer<?> ReviewCollectorService;
    protected static GenericContainer<?> ReviewAnalyzerService;

    static protected final String BUCKET_NAME = "review-analysis-bucket";
    static protected final String QUEUE_NAME = "review-analysis-queue";

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

        ReviewCollectorService = createReviewCollectorServiceContainer(8080);
        ReviewAnalyzerService = createReviewAnalyzerServiceContainer(8081);

        Startables.deepStart(ReviewCollectorService, ReviewAnalyzerService).join();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.bucket", () -> BUCKET_NAME);
        registry.add("app.queue", () -> QUEUE_NAME);
    }


    private static GenericContainer<?> createReviewCollectorServiceContainer(int port) {
        final var reviewCollectorImage = Optional.ofNullable(
                System.getenv("REVIEW_COLLECTOR_DOCKER_IMAGE"))
                .orElse("teixeirafernando/review-collector:latest");

        return new GenericContainer<>(reviewCollectorImage)
                .withEnv("AWS_ENDPOINT", "http://localstack:4566")
                .withExposedPorts(port)
                .withNetwork(SHARED_NETWORK)
                .withNetworkAliases("review-collector-service")
                .withCreateContainerCmdModifier(
                        cmd -> cmd.withHostConfig(
                                new HostConfig()
                                        .withNetworkMode(SHARED_NETWORK.getId())
                                        .withPortBindings(new PortBinding(
                                                Ports.Binding.bindPort(port),
                                                new ExposedPort(port)
                                        ))
                        )
                )
                .waitingFor(
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                )
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Review-collector-service")));
    }

    private static GenericContainer<?> createReviewAnalyzerServiceContainer(int port) {
        final var reviewAnalyzerImage = Optional.ofNullable(
                System.getenv("REVIEW_ANALYZER_DOCKER_IMAGE"))
                .orElse("teixeirafernando/review-analyzer:latest");

        return new GenericContainer<>(reviewAnalyzerImage)
                .withEnv("AWS_ENDPOINT", "http://localstack:4566")
                .withExposedPorts(port)
                .withNetwork(SHARED_NETWORK)
                .withNetworkAliases("review-analyzer-service")
                .withCreateContainerCmdModifier(
                        cmd -> cmd.withHostConfig(
                                new HostConfig()
                                        .withNetworkMode(SHARED_NETWORK.getId())
                                        .withPortBindings(new PortBinding(
                                                Ports.Binding.bindPort(port),
                                                new ExposedPort(port)
                                        ))
                        )
                )
                .waitingFor(
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                )
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Review-analyzer-service")));
    }

}


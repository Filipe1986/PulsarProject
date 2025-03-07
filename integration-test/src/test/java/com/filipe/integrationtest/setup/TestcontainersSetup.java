package com.filipe.integrationtest.setup;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;


@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersSetup {


    @Bean
    public PulsarContainer pulsarContainer() {
        return new PulsarContainer(
                DockerImageName.parse("apachepulsar/pulsar:latest"))
                .withFunctionsWorker()

                .withCopyFileToContainer(
                        MountableFile.forHostPath(
                                Path.of("../PulsarFunctions/target/PulsarFunctions-0.0.1-SNAPSHOT.jar").toAbsolutePath()),
                        "/pulsar/functions/java-functions.jar"
                )
                .withCommand("/pulsar/bin/pulsar standalone")
                .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
                .waitingFor(Wait.forLogMessage(".*Function worker service started.*", 1));

    }

    @Bean
    public PulsarClient pulsarClient(PulsarContainer pulsarContainer) throws Exception {
        return PulsarClient.builder()
                .serviceUrl(pulsarContainer.getPulsarBrokerUrl())
                .build();
    }

    @Bean
    public PulsarAdmin pulsarAdmin(PulsarContainer pulsarContainer) throws Exception {
        return  PulsarAdmin.builder()
                .serviceHttpUrl(pulsarContainer.getHttpServiceUrl())
                .build();

    }





}
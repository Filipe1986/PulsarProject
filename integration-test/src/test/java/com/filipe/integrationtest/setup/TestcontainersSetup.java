package com.filipe.integrationtest.setup;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;
import java.util.Collections;


@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersSetup {

    public final String PULSAR_TOPIC = "pulsar-project";
    @Bean
    public PulsarContainer pulsarContainer() {
        PulsarContainer container = new PulsarContainer(
                DockerImageName.parse("apachepulsar/pulsar:latest"))
                .withFunctionsWorker()
                .withExposedPorts(8080, 6650);

        container.start();
        return container;
    }

    @Bean
    public PulsarClient pulsarClient(PulsarContainer pulsarContainer) throws Exception {
        return PulsarClient.builder()
                .serviceUrl(pulsarContainer.getPulsarBrokerUrl())
                .build();
    }

    @Bean
    public PulsarAdmin pulsarAdmin(PulsarContainer pulsarContainer) throws Exception {
        PulsarAdmin pulsarAdmin = PulsarAdmin.builder()
                .serviceHttpUrl(pulsarContainer.getHttpServiceUrl())
                .build();
        deployFunction(pulsarAdmin, "process", "MultiplyByThreeFunction",
                Path.of("target/PulsarFunctions-1.0.0-SNAPSHOT.jar"));

        return pulsarAdmin;
    }



    public void deployFunction(PulsarAdmin admin, String functionName,
                               String className, Path functionPackagePath) throws Exception {


        admin.functions().createFunction(
                FunctionConfig.builder()
                        .tenant("public")
                        .namespace("default")
                        .name(functionName)
                        .className(className)
                        .inputs(Collections.singleton(PULSAR_TOPIC))
                        .output("output-topic")
                        .build(),
                String.valueOf(functionPackagePath.toFile())
        );
    }
}
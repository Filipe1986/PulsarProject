package com.filipe.integrationtest;


import com.filipe.PulsarNumber;
import com.filipe.application.PulsarProjectApplication;
import com.filipe.integrationtest.setup.TestcontainersSetup;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.PulsarContainer;

import java.io.IOException;


@Slf4j
@Import(TestcontainersSetup.class)
@SpringBootTest(classes = PulsarProjectApplication.class)
class PulsarProjectApplicationTests {


    @Autowired
    PulsarContainer pulsarContainer;

    public static final String PULSAR_TOPIC = "pulsar-project";
    public static final String PULSAR_TOPIC_OUTBOUND = "pulsar-project" + "-outbound";

    String subscriptionName = "test-subscription";


    @Test
    void contextLoads() throws IOException, InterruptedException {


        Container.ExecResult execResult = pulsarContainer.execInContainer(
                "bin/pulsar-admin", "functions", "create",
                "--tenant", "public",
                "--namespace", "default",
                "--name", "multiply-by-three",
                "--inputs", PULSAR_TOPIC,
                "--output", PULSAR_TOPIC_OUTBOUND,
                "--classname", "com.filipe.MultiplyByThreeFunction",
                "--jar", "/pulsar/functions/java-functions.jar"
        );

        log.info("Result: {}", execResult.getStdout());

        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl(pulsarContainer.getPulsarBrokerUrl())
                .build();

        Producer<PulsarNumber> numberProducer = pulsarClient
                .newProducer(Schema.JSON(PulsarNumber.class))
                .topic(PULSAR_TOPIC)
                .create();

        Message<PulsarNumber> receive;
        try (Consumer<PulsarNumber> subscribe = pulsarClient.newConsumer(Schema.JSON(PulsarNumber.class))
                .topic(PULSAR_TOPIC_OUTBOUND)
                .subscriptionName(subscriptionName)
                .subscribe()) {

            numberProducer.send(new PulsarNumber(1));
            receive = subscribe.receive();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assertions.assertNotNull(receive);
        Assertions.assertEquals(3, receive.getValue().getNumber());

        System.out.println(receive.getValue().toString());

    }

}

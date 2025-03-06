package com.filipe.pulsarproject;


import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PulsarContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PulsarProjectApplicationTests {


    @Autowired
    PulsarContainer pulsarContainer;

    public static final String PULSAR_TOPIC = "pulsar-project";

    String subscriptionName = "test-subscription";


    @Test
    void contextLoads() throws PulsarClientException {

        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl(pulsarContainer.getPulsarBrokerUrl())
                .build();

        Producer<Number> numberProducer = pulsarClient
                .newProducer(Schema.JSON(Number.class))
                .topic(PULSAR_TOPIC)
                .create();

        Message<Number> receive;
        try (Consumer<Number> subscribe = pulsarClient.newConsumer(Schema.JSON(Number.class))
                .topic(PULSAR_TOPIC)
                .subscriptionName(subscriptionName)
                .subscribe()) {

            numberProducer.send(new Number(1));
            receive = subscribe.receive();
        }

        Assertions.assertNotNull(receive);

        System.out.println(receive.getValue().toString());

    }

}

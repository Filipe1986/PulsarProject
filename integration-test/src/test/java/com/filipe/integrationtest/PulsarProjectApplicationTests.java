package com.filipe.integrationtest;


import com.filipe.PulsarNumber;
import com.filipe.integrationtest.setup.TestcontainersSetup;

import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PulsarContainer;


@Import(TestcontainersSetup.class)
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

        Producer<PulsarNumber> numberProducer = pulsarClient
                .newProducer(Schema.JSON(PulsarNumber.class))
                .topic(PULSAR_TOPIC)
                .create();

        Message<PulsarNumber> receive;
        try (Consumer<PulsarNumber> subscribe = pulsarClient.newConsumer(Schema.JSON(PulsarNumber.class))
                .topic("output-topic")
                .subscriptionName(subscriptionName)
                .subscribe()) {

            numberProducer.send(new PulsarNumber(1));
            receive = subscribe.receive();
        }

        Assertions.assertNotNull(receive);
        Assertions.assertEquals(3, receive.getValue().getNumber());

        System.out.println(receive.getValue().toString());

    }

}

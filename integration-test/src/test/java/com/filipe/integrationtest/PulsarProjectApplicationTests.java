package com.filipe.integrationtest;


import com.filipe.PulsarNumber;
import com.filipe.application.PulsarProjectApplication;
import com.filipe.integrationtest.setup.TestcontainersSetup;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PulsarContainer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;



@Slf4j
@Import(TestcontainersSetup.class)
@SpringBootTest(classes = PulsarProjectApplication.class)
class PulsarProjectApplicationTests {


    @Autowired
    PulsarContainer pulsarContainer;

    public static final String PULSAR_TOPIC = "pulsar-project";
    public static final String PULSAR_TOPIC_OUTBOUND = "pulsar-project" + "-outbound";
    public static final String subscriptionName = "test-subscription";



    @RepeatedTest(1)
    void contextLoads() throws IOException, PulsarAdminException {

        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl(pulsarContainer.getPulsarBrokerUrl())
                .build();

        Producer<PulsarNumber> numberProducer = pulsarClient
                .newProducer(Schema.JSON(PulsarNumber.class))
                .topic(PULSAR_TOPIC)
                .create();


        Consumer<PulsarNumber> subscribeInbound = pulsarClient.newConsumer(Schema.JSON(PulsarNumber.class))
                .topic(PULSAR_TOPIC)
                .subscriptionName(subscriptionName + "-inbound")
                .subscribe();

        numberProducer.send(new PulsarNumber(2));
        numberProducer.send(new PulsarNumber(2));
        Message<PulsarNumber> receiveInbound = subscribeInbound.receive(3, TimeUnit.SECONDS);
        Assertions.assertEquals(2,receiveInbound.getValue().getNumber());
        Message<PulsarNumber> receiveInbound1 = subscribeInbound.receive(3, TimeUnit.SECONDS);
        Assertions.assertEquals(2,receiveInbound1.getValue().getNumber());

        log.info("Received inbound message: {}", receiveInbound.getValue());


        PulsarAdmin pulsarAdmin = PulsarAdmin.builder()
                .serviceHttpUrl(pulsarContainer.getHttpServiceUrl())
                .build();

        List<String> topics = pulsarAdmin.topics().getList("public/default");
        topics.forEach(topic -> log.info("Topic: {}", topic));




        createFunctionWithRetry(pulsarAdmin);


        Consumer<PulsarNumber> subscribe = pulsarClient.newConsumer(Schema.JSON(PulsarNumber.class))
                .topic(PULSAR_TOPIC_OUTBOUND)
                .subscriptionName(subscriptionName)
                .subscribe();

            CompletableFuture.runAsync(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        numberProducer.sendAsync(new PulsarNumber(1));
                        log.info("Message sent 1");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            Message<PulsarNumber> receive = subscribe.receive(20, TimeUnit.SECONDS);
            Assertions.assertEquals(3, receive.getValue().getNumber());

            numberProducer.sendAsync(new PulsarNumber(1));
            receive = subscribe.receive(10, TimeUnit.SECONDS);
            Assertions.assertEquals(3, receive.getValue().getNumber());

            numberProducer.sendAsync(new PulsarNumber(2));
            receive = subscribe.receive(10, TimeUnit.SECONDS);
            Assertions.assertEquals(6, receive.getValue().getNumber());

            numberProducer.sendAsync(new PulsarNumber(3));
            receive = subscribe.receive(10, TimeUnit.SECONDS);
            Assertions.assertEquals(9, receive.getValue().getNumber());

    }

    private void createFunctionWithRetry(PulsarAdmin pulsarAdmin) {


        FunctionConfig functionConfig = getFunctionConfig();
        int maxRetries = 10;
        int retryCount = 0;
        int backoffMs = 2000;

        while (retryCount < maxRetries) {
            try {
                Thread.sleep(backoffMs);
                pulsarAdmin.functions().createFunctionAsync(functionConfig, functionConfig.getJar()).get();
                log.info("Function created successfully");
                return;
            } catch (Exception e) {
                retryCount++;
            }
        }
    }

    private static @NotNull FunctionConfig getFunctionConfig() {
        FunctionConfig functionConfig = new FunctionConfig();
        functionConfig.setTenant("public");
        functionConfig.setNamespace("default");
        functionConfig.setName("multiply-by-three");
        functionConfig.setInputs(Collections.singleton(PULSAR_TOPIC));
        functionConfig.setOutput(PULSAR_TOPIC_OUTBOUND);
        functionConfig.setClassName("com.filipe.MultiplyByThreeFunction");
        functionConfig.setRuntime(FunctionConfig.Runtime.JAVA);
        String jarPath = "../PulsarFunctions/target/PulsarFunctions-0.0.1-SNAPSHOT.jar";
        functionConfig.setJar(jarPath);
        return functionConfig;
    }

}

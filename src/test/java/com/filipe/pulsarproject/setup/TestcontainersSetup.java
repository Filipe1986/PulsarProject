package com.filipe.pulsarproject.setup;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersSetup {

    @Bean
    PulsarContainer pulsarContainer() {
        return new PulsarContainer(DockerImageName.parse("apachepulsar/pulsar:latest"));
    }
}

package com.filipe.integrationtest;


import com.filipe.application.PulsarProjectApplication;
import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestPulsarProjectApplication {

    public static void main(String[] args) {
        SpringApplication.from(PulsarProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}

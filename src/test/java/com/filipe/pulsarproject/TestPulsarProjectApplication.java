package com.filipe.pulsarproject;

import org.springframework.boot.SpringApplication;

public class TestPulsarProjectApplication {

    public static void main(String[] args) {
        SpringApplication.from(PulsarProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}

package com.filipe;


import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;


@Slf4j
public class MultiplyByThreeFunction implements Function<PulsarNumber, PulsarNumber> {

    @Override
    public PulsarNumber process(PulsarNumber input, Context context) {
        log.info("Processing pulsar number {}", input);
        log.info("Context {}: ", context);
        if (input == null) {
            return null;
        }
        return new PulsarNumber(input.getNumber() * 3);
    }
}
package com.filipe;


import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;


public class MultiplyByThreeFunction implements Function<PulsarNumber, PulsarNumber> {

    @Override
    public PulsarNumber process(PulsarNumber input, Context context) {
        if (input == null) {
            return null;
        }
        return new PulsarNumber(input.getNumber() * 3);
    }
}
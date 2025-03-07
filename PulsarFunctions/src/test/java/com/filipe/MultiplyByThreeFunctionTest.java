package com.filipe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiplyByThreeFunctionTest {

    @Test
    void process() {
        MultiplyByThreeFunction multiplyByThreeFunction = new MultiplyByThreeFunction();
        PulsarNumber number = new PulsarNumber(3);
        PulsarNumber result = multiplyByThreeFunction.process(number, null);
        assertEquals(9, result.getNumber());
    }

    @Test
    void processNull() {
        MultiplyByThreeFunction multiplyByThreeFunction = new MultiplyByThreeFunction();
        PulsarNumber result = multiplyByThreeFunction.process(null, null);
        assertNull(result);
    }

}
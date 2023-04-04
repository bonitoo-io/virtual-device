package io.bonitoo.qa.data.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumGeneratorTest {

    static IntStream genInts(){
        return IntStream.range(-10, 100);
    }

    @ParameterizedTest
    @MethodSource("genInts")
    public void basicGeneratorTest(Integer period){
        double max = 10;
        double min = 0;
        double result = NumGenerator.genDoubleVal(period, min, max, System.currentTimeMillis());
        assertTrue(result <= max && result >= min);
    }

    @Test
    public void period0Test(){
        double zeroResult = NumGenerator.genDoubleVal(0, -10, 10, System.currentTimeMillis());
        assertEquals(0, zeroResult);
        double midNegResult = NumGenerator.genDoubleVal(0, -15, -5, System.currentTimeMillis());
        assertEquals(-10, midNegResult);
        double midPosResult = NumGenerator.genDoubleVal(0, 10, 50, System.currentTimeMillis());
        assertEquals(30, midPosResult);
    }
}

package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.VirDevConfigException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.DoubleSupplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
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

    static DoubleStream genDoubles(){

        double[] vals = new double[10];
        vals[0] = 0.01;
        for(int i = 1; i < vals.length; i++){
            vals[i] = vals[i-1] + 0.1;
        }

        return DoubleStream.of(vals);

    }

    static LongStream genMillis(){
        long[] vals = new long[5];
        long millis6H = 6 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        for(int i = -2; i < vals.length - 2; i++){
            vals[i+2] = now + (millis6H * i);
        }
        return LongStream.of(vals);
    }

    static Stream<Arguments> paramStream(){
        double[] doubles = genDoubles().toArray();
        long[] millis = genMillis().toArray();
        Arguments[] args = new Arguments[doubles.length * millis.length];
        int index = 0;
        for(double d : doubles){
            for(long l : millis){
                args[index++] = Arguments.of(d, l);
            }
        }
        return Stream.of(args);
    }

    @ParameterizedTest
    @MethodSource("paramStream")
    public void basicDoubleGenTest(Double val, Long millis){

        double max = 8;
        double min = -4;
        double spread = max - min;
        double spreadMin = min - (spread * val);
        double spreadMax = max + (spread * val);

        long millis6H = 6 * 60 * 60 * 1000;
        long millis12H = 12 * 60 * 60 * 1000;

        double result = NumGenerator.genDoubleValSin(1.0, val, min, max, millis);

 /*       System.out.printf("DEBUG val: %.3f, result: %.6f, spread: %.3f, spreadMin: %.3f, spreadMax: %.3f, millis: %d \n",
          val,
          result,
          spread,
          spreadMin,
          spreadMax,
          millis); */

        assertTrue(result <= spreadMax && result >= spreadMin);

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

    @Test
    public void dev0GenSinTest(){
        double zeroResult = NumGenerator.genDoubleValSin(1, 0, -10, 10, System.currentTimeMillis());
        System.out.println("DEBUG zeroResult " + zeroResult );
        assertEquals(0, zeroResult);
        double midNegResult = NumGenerator.genDoubleValSin(1, 0, -15, -5, System.currentTimeMillis());
        System.out.println("DEBUG midNegResult " + midNegResult);
        assertEquals(-10, midNegResult);
        double midPosResult = NumGenerator.genDoubleValSin(1, 0, 10, 50, System.currentTimeMillis());
        System.out.println("DEBUG midPosResult " + midPosResult);
        assertEquals(30, midPosResult);
    }

    @Test
    public void devIllegalGenSinTest(){
        Exception exp1 = assertThrows(VirtualDeviceRuntimeException.class, () -> {
            double illegalNeg = NumGenerator.genDoubleValSin(1, -1, -0.5, 1.5, System.currentTimeMillis());
        });
        assertEquals("The deviation value 'dev' is -1.000.  But must be between 0.0 and 1.0", exp1.getMessage());

        Exception exp2 = assertThrows(VirtualDeviceRuntimeException.class, () -> {
            double illegalPos = NumGenerator.genDoubleValSin(1, Math.PI, -0.5, 1.5, System.currentTimeMillis());
        });
        assertEquals("The deviation value 'dev' is 3.142.  But must be between 0.0 and 1.0", exp2.getMessage());


    }
}

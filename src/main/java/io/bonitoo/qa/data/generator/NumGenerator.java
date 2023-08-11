package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;

import java.time.LocalDateTime;

/**
 * Generates random numerical values based on a sinusoidal attractor.
 *
 * <p>The core method is genDoubleVal().  Other methods simply call
 * this method with reasonable bounding and frequency values.
 */
public class NumGenerator extends DataGenerator {

  private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
  private static final long MONTH_MILLIS = DAY_MILLIS * 30;

  public static final double DEFAULT_DEV = 0.25;

  public static long millisSince0Hour() {
    LocalDateTime now = LocalDateTime.now();
    return (((now.getHour() * 60 * 60) + (now.getMinute() * 60) + now.getSecond()) * 1000) + (now.getNano() / 1000000);
  }

  public static double pctOfDay(long millis) {
    return (double) millis / (double) DAY_MILLIS;
  }

  public static double radiansOfClock(long millis) {
    return 2 * Math.PI * pctOfDay(millis);
  }

  /**
   * Generates a random double value based on a sinusoidal curve and a
   * normal distribution centered around the spread between max and min.
   *
   * <p>The sinusoidal curve begins at midnight and leads to a "local attractor" value,
   * calculated based on the millisecond time parameter, which then determines the
   * radians of elapsed time on a 24 hour clock.  This value then centers a
   * local spread upon which a random value based on a normal distribution is generated.</p>
   *
   * @param period - number of cycles executed in one day.  Note that
   *               negative numbers reverse the starting sense of the curve.
   *               Values between 0 and 1 can be used to define a period of
   *               longer than one day.  e.g. 0.5 implies two days to complete
   *               a cycle.
   * @param dev - standard deviation from 0.0 to 1.0 to be used in generating a value.
   *            Smaller deviations lead to narrower bands of generated values, and
   *            larger deviations lead to wider bands.
   * @param min - approximate minimum value.  This can be exceeded by up to the deviation
   *            value applied to the spread.
   * @param max - approximate maximum value.  This can be exceeded by up to the deviation
   *            value applied to the spread.
   * @param time - in milliseconds since midnight at which the value should be generated.
   * @return - a random value.
   */
  public static double genDoubleValSin(double period,
                                       double dev,
                                       double min,
                                       double max,
                                       long time) {

    if (dev < 0 || dev > 1) {
      throw new VirtualDeviceRuntimeException(
        String.format("The deviation value 'dev' is %.3f.  But must be between 0.0 and 1.0",
          dev)
      );
    }

    if (period == 0 || dev == 0) {
      if (0 < min || 0 > max) {
        return (max + min) / 2;
      } else {
        return 0;
      }
    }

    // Use a negative period value (-1) to get set similar to temperature model
    // the lowest values occur around 06h and highest values around 18h
    // on or around the equinox.
    double attr = Math.sin(radiansOfClock(time) * period)/2.0 + 0.5;
    double spread = max - min;

    // Narrows down the band to take into account standard dev
    double sigmaFictif = dev <= 0 ? spread * DEFAULT_DEV : spread * dev;
    double localMax = ((attr * spread) + sigmaFictif) + min;
    double localMin = ((attr * spread) - sigmaFictif) + min;

    return gaussNormalFilter(localMin, localMax);
  }

  /**
   * Generates a random double value.
   *
   * @param period - sinusoidal period used for the model.
   * @param min - minimum possible value.
   * @param max - maximum possible value.
   * @param time - time in milliseconds for which the value is to be generated.
   * @return - the generated double value.
   */
  public static double genDoubleVal(double period, double min, double max, long time) {

    if (period == 0) {
      if (0 < min || 0 > max) {
        return (max + min) / 2;
      } else {
        return 0;
      }
    }

    double dp = (double) period;
    final double diff = max - min;
    final double periodVal = (diff / 4.0)
        * Math.sin(((time / DAY_MILLIS) % period / dp) * 2.0 * Math.PI);
    final double dayVal = (diff / 4.0)
        * Math.sin(((time % DAY_MILLIS) / DAY_MILLIS) * 2 * Math.PI - Math.PI / 2);
    return min + diff / 2 + periodVal + dayVal + Math.random();
  }

  public static double genTemperature(long time) {
    return (long) (genDoubleVal(30, 0, 40, time) * 1e1) / 1e1;
  }

  public static double genHumidity(long time) {
    return (long) (genDoubleVal(10, 0, 99, time) * 1e1) / 1e1;
  }

  public static double genPressure(long time) {
    return (long) (genDoubleVal(20, 970, 1050, time) * 1e1) / 1e1;
  }

  public static double genCo2(long time) {
    return (long) (genDoubleVal(1, 400, 3000, time) * 1e2) / 1e2;
  }

  public static double genTvoc(long time) {
    return (long) (genDoubleVal(1, 250, 2000, time) * 1e2) / 1e2;
  }

  public static double precision(double val, double prec) {
    return (long) (val * prec) / prec;
  }

  @Override
  public Object genData() {
    ItemConfig conf = item.getConfig();
    switch (conf.getType()) {
      case BuiltInTemp:
        return genTemperature(System.currentTimeMillis());
      case Double:
/*
        return genDoubleVal(
          ((ItemNumConfig) conf).getPeriod(),
          ((ItemNumConfig) conf).getMin(),
          ((ItemNumConfig) conf).getMax(),
          System.currentTimeMillis());

 */

/*
        System.out.println("DEBUG generating Double from " + ((ItemNumConfig)conf));
        double d = genDoubleValSin(
          ((ItemNumConfig) conf).getPeriod(),
          ((ItemNumConfig) conf).getDev(),
          ((ItemNumConfig) conf).getMin(),
          ((ItemNumConfig) conf).getMax(),
          System.currentTimeMillis());
        System.out.println("DEBUG generated d := " + d);
        return d;
*/
        return genDoubleValSin(
          ((ItemNumConfig) conf).getPeriod(),
          ((ItemNumConfig) conf).getDev(),
          ((ItemNumConfig) conf).getMin(),
          ((ItemNumConfig) conf).getMax(),
          System.currentTimeMillis());

      case Long:
/*
        return Math.round(genDoubleVal(
          ((ItemNumConfig) conf).getPeriod(),
          ((ItemNumConfig) conf).getMin(),
          ((ItemNumConfig) conf).getMax(),
          System.currentTimeMillis())
        );

 */
        System.out.println("DEBUG generating Long");
        return Math.round(genDoubleValSin(
          ((ItemNumConfig) conf).getPeriod(),
          ((ItemNumConfig) conf).getDev(),
          ((ItemNumConfig) conf).getMin(),
          ((ItemNumConfig) conf).getMax(),
          System.currentTimeMillis())
        );

      default:
        throw new VirtualDeviceRuntimeException(
          String.format("ItemType %s not supported by NumGenerator", conf.getType())
        );
    }
  }

  private static final double CHANCE_3SIGMA = 0.010;
  private static final double CHANCE_2_5SIGMA = 0.037;
  private static final double CHANCE_2SIGMA = 0.080;
  private static final double CHANCE_1_5SIGMA = 0.190;
  private static final double CHANCE_1SIGMA = 0.300;
  private static final double CHANCE_0_5SIGMA = 0.380;
  private static final double CHANCE_OUTLIER = 0.003;

  public static double gaussNormalFilter(double min, double max){
    double spread = max - min;
    double mid = spread / 2;
    double sigma3Max = spread * 0.997;
    double sigma3Min = max - sigma3Max;
    double sigma2_5Max = spread * 0.987;
    double sigma2_5Min = max - sigma2_5Max;
    double sigma2Max = spread * 0.95;
    double sigma2Min = max - sigma2Max;
    double sigma1_5Max = spread * 0.87;
    double sigma1_5Min = max - sigma1_5Max;
    double sigma1Max = spread * 0.68;
    double sigma1Min = max - sigma1Max;
    double sigma0_5Max = max - (spread * 0.38);
    double sigma0_5Min = max - sigma0_5Max;

    double d = (Math.random() * spread) + min;

    while (true) {
      double check = Math.random();
      if ((d > sigma0_5Min && d < sigma0_5Max)
          && check < CHANCE_0_5SIGMA) {
        break;
      } else if (((d > sigma1Min && d < sigma0_5Min)
          || (d < sigma1Max && d > sigma0_5Max))
          && check < CHANCE_1SIGMA) {
        break;
      } else if (((d > sigma1_5Min && d < sigma1Min)
          || (d < sigma1_5Max && d > sigma1Max))
          && check < CHANCE_1_5SIGMA) {
        break;
      } else if (((d > sigma2Min && d < sigma1_5Min)
          || (d < sigma2Max && d > sigma2_5Max))
          && check < CHANCE_2SIGMA) {
        break;
      } else if (((d > sigma2_5Min && d < sigma2Min)
          || (d < sigma2_5Max && d > sigma2Max))
          && check < CHANCE_2_5SIGMA) {
        break;
      } else if (((d > sigma3Min && d < sigma2_5Min)
          || (d < sigma3Max && d > sigma2_5Max))
          && check < CHANCE_3SIGMA) {
        break;
      }else if (check < CHANCE_OUTLIER) {
        break;
      }
      d = (Math.random() * spread) + min;
    }

    return d;
  }


}

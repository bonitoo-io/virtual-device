package io.bonitoo.qa.data.generator;

import io.bonitoo.qa.VirtualDeviceRuntimeException;
import io.bonitoo.qa.conf.data.DataConfig;
import io.bonitoo.qa.conf.data.ItemConfig;
import io.bonitoo.qa.conf.data.ItemNumConfig;
import java.time.LocalDateTime;

/**
 * Generates random numerical values based on a sinusoidal attractor.
 *
 * <p>The core method is genDoubleValSin().  Other methods simply call
 * this method with reasonable bounding, frequency(period) and deviation values.
 */

public class NumGenerator extends DataGenerator<DataConfig> {

  private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

  public static final double DEFAULT_DEV = 0.25;

  /**
   * Calculates the number of milliseconds elapsed since midnight.
   *
   * @return - milliseconds elapsed since midnight.
   */
  public static long millisSince0Hour() {
    LocalDateTime now = LocalDateTime.now();
    return (((now.getHour() * 60 * 60)
      + (now.getMinute() * 60)
      + now.getSecond()) * 1000) + (now.getNano() / 1000000);
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
    double attr = Math.sin(radiansOfClock(time) * period) / 2.0 + 0.5;
    double spread = max - min;

    // Narrows down the band to take into account standard dev
    double sigmaFictif = spread * dev;
    double localMax = ((attr * spread) + sigmaFictif) + min;
    double localMin = ((attr * spread) - sigmaFictif) + min;

    return gaussNormalFilter(localMin, localMax);
  }

  @Override
  public Object genData() {
    ItemConfig conf = item.getConfig();
    switch (conf.getType()) {
      case Double:
        return genDoubleValSin(
          ((ItemNumConfig) conf).getPeriod(),
          ((ItemNumConfig) conf).getDev(),
          ((ItemNumConfig) conf).getMin(),
          ((ItemNumConfig) conf).getMax(),
          System.currentTimeMillis());

      case Long:
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

  /**
   * Generates a random double based on the spread between min and max
   * and using a normal gaussian distribution of possible values.
   *
   * @param min - minimum target value.
   * @param max - maximum target value.
   * @return - random double value.
   */
  public static double gaussNormalFilter(double min, double max) {
    double spread = max - min;
    double mid = spread / 2;
    double sigma3Max = spread * 0.997;
    double sigma3Min = max - sigma3Max;
    double sigma2dot5Max = spread * 0.987;
    double sigma2dot5Min = max - sigma2dot5Max;
    double sigma2Max = spread * 0.95;
    double sigma2Min = max - sigma2Max;
    double sigma1dot5Max = spread * 0.87;
    double sigma1dot5Min = max - sigma1dot5Max;
    double sigma1Max = spread * 0.68;
    double sigma1Min = max - sigma1Max;
    double sigma0dot5Max = max - (spread * 0.38);
    double sigma0dot5Min = max - sigma0dot5Max;

    double d = (Math.random() * spread) + min;

    while (true) {
      double check = Math.random();
      if ((d > sigma0dot5Min && d < sigma0dot5Max)
          && check < CHANCE_0_5SIGMA) {
        break;
      } else if (((d > sigma1Min && d < sigma0dot5Min)
          || (d < sigma1Max && d > sigma0dot5Max))
          && check < CHANCE_1SIGMA) {
        break;
      } else if (((d > sigma1dot5Min && d < sigma1Min)
          || (d < sigma1dot5Max && d > sigma1Max))
          && check < CHANCE_1_5SIGMA) {
        break;
      } else if (((d > sigma2Min && d < sigma1dot5Min)
          || (d < sigma2Max && d > sigma2dot5Max))
          && check < CHANCE_2SIGMA) {
        break;
      } else if (((d > sigma2dot5Min && d < sigma2Min)
          || (d < sigma2dot5Max && d > sigma2Max))
          && check < CHANCE_2_5SIGMA) {
        break;
      } else if (((d > sigma3Min && d < sigma2dot5Min)
          || (d < sigma3Max && d > sigma2dot5Max))
          && check < CHANCE_3SIGMA) {
        break;
      } else if (check < CHANCE_OUTLIER) {
        break;
      }
      d = (Math.random() * spread) + min;
    }
    return d;
  }
}

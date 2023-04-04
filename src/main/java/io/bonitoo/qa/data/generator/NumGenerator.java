package io.bonitoo.qa.data.generator;

/**
 * Generates random numerical values based on a sinusoidal attractor.
 *
 * <p>The core method is genDoubleVal().  Other methods simply call
 * this method with reasonable bounding and frequency values.
 */
public class NumGenerator {

  private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
  private static final long MONTH_MILLIS = DAY_MILLIS * 30;

  /**
   * Generates a random double value.
   *
   * @param period - sinusoidal period used for the model.
   * @param min - minimum possible value.
   * @param max - maximum possible value.
   * @param time - time in milliseconds for which the value is to be generated.
   * @return - the generated double value.
   */
  public static double genDoubleVal(long period, double min, double max, long time) {

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
}

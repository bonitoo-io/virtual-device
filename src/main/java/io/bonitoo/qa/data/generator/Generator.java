package io.bonitoo.qa.data.generator;

public class Generator {

    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
    private static final long MONTH_MILLIS = DAY_MILLIS * 30;
    public static double genDoubleVal(long period, double min, double max, long time){

        if(period == 0){
            if(0 < min || 0 > max ) {
                return (max + min) / 2;
            }
            else {
                return 0;
            }
        }

        double dPeriod = (double)period;
        final double diff = max - min;
        final double periodVal = (diff / 4.0) * Math.sin(((time / DAY_MILLIS) % period / dPeriod) * 2.0 * Math.PI);
        final double dayVal = (diff / 4.0) * Math.sin(((time % DAY_MILLIS) / DAY_MILLIS) * 2 * Math.PI - Math.PI / 2 );
        // todo finish
        return min + diff / 2 + periodVal + dayVal + Math.random();
    }

    public static double genTemperature(long time){
        return (long)(genDoubleVal(30, 0, 40, time) * 1e1) / 1e1 ;
    }

    public static double genHumidity(long time){
        return (long)(genDoubleVal(10, 0, 99, time) * 1e1) / 1e1;
    }

    public static double genPressure(long time){
        return (long)(genDoubleVal(20, 970, 1050, time) * 1e1) / 1e1;
    }

    public static double genCO2(long time){
        return (long)(genDoubleVal(1, 400, 3000, time) * 1e2) / 1e2;
    }

    public static double genTVOC(long time){
        return (long)(genDoubleVal(1, 250, 2000, time) * 1e2) / 1e2;
    }

    public static double precision(double val, double prec){
        return (long)(val * prec) / prec;
    }
}

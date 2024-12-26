package trial2claude;

public class Probability {
    private static final double E = Math.E;
    private static final double PI = Math.PI;
    
    public static double weibull(double x, double lambda, double k) {
        if (x < 0) return 0.0;
        return (k / lambda) * Math.pow(x / lambda, k - 1) * Math.exp(-Math.pow(x / lambda, k));
    }
    
    public static double exponential(double x, double mean) {
        if (x < 0) return 0.0;
        double lambda = 1.0 / mean;
        return lambda * Math.exp(-lambda * x);
    }
    
    public static double generateWeibullRandom(double lambda, double k) {
        double u = Math.random();
        return lambda * Math.pow(-Math.log(1 - u), 1.0/k);
    }
    
    public static double generateExponentialRandom(double mean) {
        double u = Math.random();
        return -mean * Math.log(1 - u);
    }
}

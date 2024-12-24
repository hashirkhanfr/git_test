package trial1;

import java.util.Random;

//--- Utility Class for Probability Distributions ---
public class ProbabilityDistributions {
 private static final Random random = new Random();

 public static double weibullPDF(double x, double lambda, double k) {
     if (x < 0) {
         return 0.0;
     }
     return (k / lambda) * Math.pow(x / lambda, k - 1) * Math.exp(-Math.pow(x / lambda, k));
 }

 public static double betaFunction(double p, double q) {
     return Math.exp(gammaLn(p) + gammaLn(q) - gammaLn(p + q));
 }

 public static double pearsonVIDensity(double x, double beta, double alpha, double p, double q) {
     if (x < 0) {
         return 0.0;
     }
     double betaValue = betaFunction(p, q);
     return (Math.pow(x, alpha - 1) / Math.pow(beta, alpha) / betaValue) *
             Math.pow(1 + (x / beta), -(p + q));
 }

 public static double exponentialDensity(double x, double mean) {
     if (x < 0) {
         return 0.0;
     }
     double lambda = 1.0 / mean;
     return lambda * Math.exp(-lambda * x);
 }

 private static double gammaLn(double x) {
     double[] coefficients = {
             76.18009172947146, -86.50532032941677,
             24.01409824083091, -1.231739572450155,
             0.001208650973866179, -0.000005395239384953
     };
     double y = x + 1;
     double tmp = y + 5.5;
     tmp -= (y + 0.5) * Math.log(tmp);
     double ser = 1.000000000190015;
     for (int j = 0; j < coefficients.length; j++) {
         ser += coefficients[j] / ++x;
     }
     return -tmp + Math.log(2.5066282746310005 * ser);
 }
}

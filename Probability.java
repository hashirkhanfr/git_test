package trialCLASSES;

public class Probability {
    
    // Beta function implementation for Pearson VI distribution
    public static double betaFunction(double p, double q) {
        return Math.exp(gammaLn(p) + gammaLn(q) - gammaLn(p + q));
    }
    
    // Gamma function logarithm implementation
    public static double gammaLn(double x) {
        double[] coef = {76.18009172947146, -86.50532032941677,
                        24.01409824083091, -1.231739572450155,
                        0.1208650973866179E-2, -0.5395239384953E-5};
        double stp = 2.5066282746310005;
        double ser = 1.000000000190015;
        double temp = x + 5.5;
        temp = (x + 0.5) * Math.log(temp) - temp;
        for (int j = 0; j < 6; j++) {
            ser += coef[j] / (x + j + 1.0);
        }
        return temp + Math.log(stp * ser / x);
    }
    
    
    public static double pearsonVI(double x, double beta, double p, double q) {
        if (x <= 0) {
            return 0; // Probability density is 0 for x <= 0
        }
        
        // Implementation following the formula:
        // f(x) = (x/β)^(p-1) / (β * [1 + (x/β)]^(p+q) * B(p,q))
        double xOverBeta = x / beta;
        double numerator = Math.pow(xOverBeta, p - 1);
        double denominator = beta * Math.pow(1 + xOverBeta, p + q) * betaFunction(p, q);
        
        return numerator / denominator;
    }
    
    // Post-discharge time density (Exponential distribution)
    public static double pddt(double x, double mean) {
        if (x < 0) return 0.0;
        double mu = 1.0 / mean; 
        return mu * Math.exp(-mu * x);
    }
    
    // Generate random treatment time using Pearson VI distribution
    public static double generateTreatmentTime(double currentTime, int category) {
        // Base parameters from the formula
        double beta = 355;   // Scale parameter
        double p = 1.64;     // First shape parameter (previously incorrectly set as alpha)
        double q = 5.72;     // Second shape parameter
        
        // Adjust parameters based on category
        switch(category) {
            case 1: // Critical cases need longer treatment
                beta *= 1.2;
                break;
            case 2:
                beta *= 1.1;
                break;
            case 3:
                // Use default parameters
                break;
            case 4:
            case 5: // Minor cases need less time
                beta *= 0.6;
                break;
        }
        
        // Use rejection sampling to generate random value
        double maxY = pearsonVI(beta, beta, p, q);
        while(true) {
            double x = Math.random() * beta * 3; // Range: [0, 3*beta]
            double y = Math.random() * maxY;
            if (y <= pearsonVI(x, beta, p, q)) {
                return x;
            }
        }
    }
    
    // Generate random post-treatment time
    public static double generatePostTreatmentTime(double currentTime) {
        double mean = 156; // From C++ implementation
        return -mean * Math.log(1 - Math.random()); // Inverse transform sampling
    }
    
    // Generate inter arrival time using Weibull distribution
    public static double generateInterarrivalTime(double currentTime) {
        double alpha = 180; // Scale parameter
        double beta = 0.914;      // Shape parameter
        double u = Math.random();
        return alpha * Math.pow(-Math.log(1 - u), 1.0/beta);
    }
}
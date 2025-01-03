package COPY;

//New class to represent diseases
public class Disease{
	 private String name;
	 private double[] categoryProbabilities; // Probabilities for categories 1-5
	 private double arrivalRate; // Base arrival rate for this disease
	 
	 public Disease(String name, double[] categoryProbabilities, double arrivalRate) {
	     this.name = name;
	     this.categoryProbabilities = categoryProbabilities;
	     this.arrivalRate = arrivalRate;
	 }
	 
	 public String getName() { return name; }
	 public double[] getCategoryProbabilities() { return categoryProbabilities; }
	 public double getArrivalRate() { return arrivalRate; }
}

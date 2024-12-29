package trial3;

import java.util.ArrayList;

public class Patient {
    private int id;
    private int category;
    private double arrivalTime;
    private double waitTime;
    private double treatmentTime;
    private double postTreatmentTime;
    private double dischargeTime;
    private int assignedBedId;
    private boolean isPreempted;
    private ArrayList<DiagnosticStudy> studies;
    
    public Patient(int id, int category, double arrivalTime) {
        this.id = id;
        this.category = category;
        this.arrivalTime = arrivalTime;
        this.studies = new ArrayList<>();
        this.assignedBedId = 0;
        this.isPreempted = false;
        
        // Generate treatment time using Pearson VI distribution
        this.treatmentTime = Probability.generateTreatmentTime(arrivalTime, category);
        
        // Generate post-treatment time using exponential distribution
        this.postTreatmentTime = Probability.generatePostTreatmentTime(arrivalTime);
        
        // Calculate recommended wait time based on category
        // Category 1: Immediate (0 min)
        // Category 2: 10 min
        // Category 3: 30 min
        // Category 4: 60 min
        // Category 5: 120 min
        this.waitTime = (category == 1) ? 0 : Math.pow(2, category - 1) * 600; // in seconds
    }
    
    public void addDiagnosticStudy(String type, double duration) {
        studies.add(new DiagnosticStudy(type, duration));
    }
    
    // Getters
    public int getId() { return id; }
    public int getCategory() { return category; }
    public double getArrivalTime() { return arrivalTime; }
    public double getWaitTime() { return waitTime; }
    public double getTreatmentTime() { return treatmentTime; }
    public double getPostTreatmentTime() { return postTreatmentTime; }
    public int getAssignedBedId() { return assignedBedId; }
    public boolean isPreempted() { return isPreempted; }
    public ArrayList<DiagnosticStudy> getStudies() { return studies; }
    public double getTotalTreatmentTime() { return treatmentTime + postTreatmentTime; }
    public double getDischargeTime() { return dischargeTime; }
    
    // Setters
    public void setWaitTime(double waitTime) { this.waitTime = waitTime; }
    public void setDischargeTime(double time) { this.dischargeTime = time; }
    public void setAssignedBedId(int id) { this.assignedBedId = id; }
    public void setPreempted(boolean preempted) { this.isPreempted = preempted; }
    
}
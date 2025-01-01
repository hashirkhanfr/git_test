package trialCLASSES;
import java.util.ArrayList;

public class Patient {
    private int id;
    private int category;
    private String disease;
    private double arrivalTime;
    private double waitTime;
    private double treatmentTime;
    private double postTreatmentTime;
    private double dischargeTime;
    private int assignedBedId;
    private boolean isPreempted;
    private ArrayList<DiagnosticStudy> studies;
    
    public Patient(int id, int category, double arrivalTime,String disease) {
        this.id = id;
        this.category = category;
        this.arrivalTime = arrivalTime;
        this.studies = new ArrayList<>();
        this.assignedBedId = 0;
        this.isPreempted = false;
        this.disease = disease;
       
        this.treatmentTime = Probability.generateTreatmentTime(arrivalTime, category); //treatment time using Pearson VI distribution
        this.postTreatmentTime = Probability.generatePostTreatmentTime(arrivalTime);  //post-treatment time using exponential distribution
        
        if (category == 1)          waitTime = 0; 		  // Immediate (0 min)
        else if (category == 2)     waitTime = 10 * 60;   // 10 minutes
        else if (category == 3)     waitTime = 30 * 60;   // 30 minutes
        else if (category == 4)     waitTime = 60 * 60;   // 60 minutes
        else if (category == 5)     waitTime = 120 * 60;  // 120 minutes
        else                        waitTime = 0;         //default for unexpected
        
    }
    
    public void addDiagnosticStudy(String type, double duration) {
        studies.add(new DiagnosticStudy(type, duration));
    }
    
    //getters
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
    
    //setters
    public void setWaitTime(double waitTime) { this.waitTime = waitTime; }
    public void setDischargeTime(double time) { this.dischargeTime = time; }
    public void setAssignedBedId(int id) { this.assignedBedId = id; }
    public void setPreempted(boolean preempted) { this.isPreempted = preempted; }
    
}
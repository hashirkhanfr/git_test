package trial2claude;

public class Patient {
    private int id;
    private int category;  // 1-5, where 1 is most urgent
    private double arrivalTime;
    private double waitTime;
    private double treatmentTime;
    private double dischargeTime;
    private String assignedBedId;
    private boolean isPreempted;
    private DiagnosticStudy[] studies;
    private int studyCount;
    private static final int MAX_STUDIES = 5;
    
    public Patient(int id, int category, double arrivalTime) {
        this.id = id;
        this.category = category;
        this.arrivalTime = arrivalTime;
        this.studies = new DiagnosticStudy[MAX_STUDIES];
        this.studyCount = 0;
        this.assignedBedId = null;
        this.isPreempted = false;
        
        // Set treatment time based on category using appropriate distribution
        if (category == 4) {
            this.treatmentTime = Probability.generateWeibullRandom(60, 2); // 1 hour mean
        } else {
            this.treatmentTime = Probability.generateExponentialRandom(
                category == 1 ? 120 : // 2 hours for critical
                category == 2 ? 90 :  // 1.5 hours for emergency
                category == 3 ? 60 :  // 1 hour for urgent
                30);                  // 30 min for non-urgent
        }
    }
    
    public void addDiagnosticStudy(String type, double duration) {
        if (studyCount < MAX_STUDIES) {
            studies[studyCount++] = new DiagnosticStudy(type, duration);
        }
    }
    
    // Getters and setters
    public int getId() { return id; }
    public int getCategory() { return category; }
    public double getArrivalTime() { return arrivalTime; }
    public double getWaitTime() { return waitTime; }
    public double getTreatmentTime() { return treatmentTime; }
    public String getAssignedBedId() { return assignedBedId; }
    public boolean isPreempted() { return isPreempted; }
    
    public void setWaitTime(double waitTime) { this.waitTime = waitTime; }
    public void setDischargeTime(double time) { this.dischargeTime = time; }
    public void setAssignedBedId(String bedId) { this.assignedBedId = bedId; }
    public void setPreempted(boolean preempted) { this.isPreempted = preempted; }
}



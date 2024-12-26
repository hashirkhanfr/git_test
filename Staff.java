package trial2claude;

public class Staff {
    private String id;
    private String role;
    private int maxPatients;
    Patient[] assignedPatients;
    private int patientCount;
    private double totalWorkTime;
    private double lastWorkStartTime;
    
    public Staff(String id, String role, int maxPatients) {
        this.id = id;
        this.role = role;
        this.maxPatients = maxPatients;
        this.assignedPatients = new Patient[maxPatients];
        this.patientCount = 0;
        this.totalWorkTime = 0;
        this.lastWorkStartTime = 0;
    }
    
    public boolean canTakePatient() {
        return patientCount < maxPatients;
    }
    
    public void assignPatient(Patient patient, double currentTime) {
        if (patientCount < maxPatients) {
            if (patientCount == 0) {
                lastWorkStartTime = currentTime;
            }
            assignedPatients[patientCount++] = patient;
        }
    }
    
    public void releasePatient(Patient patient, double currentTime) {
        for (int i = 0; i < patientCount; i++) {
            if (assignedPatients[i] == patient) {
                // Shift remaining patients
                for (int j = i; j < patientCount - 1; j++) {
                    assignedPatients[j] = assignedPatients[j + 1];
                }
                patientCount--;
                
                if (patientCount == 0) {
                    totalWorkTime += (currentTime - lastWorkStartTime);
                }
                break;
            }
        }
    }
    
    public double getUtilization(double totalTime) {
        return totalWorkTime / totalTime;
    }
    
    // Getters
    public String getId() { return id; }
    public String getRole() { return role; }
    public int getPatientCount() { return patientCount; }
}

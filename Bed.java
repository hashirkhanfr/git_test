package trial2claude;

public class Bed {
    private String id;
    private String type;
    private Patient currentPatient;
    private boolean occupied;
    private double totalOccupiedTime;
    private double lastOccupiedStartTime;
    
    public Bed(String id, String type) {
        this.id = id;
        this.type = type;
        this.occupied = false;
        this.totalOccupiedTime = 0;
        this.lastOccupiedStartTime = 0;
    }
    
    public void assignPatient(Patient patient, double currentTime) {
        this.currentPatient = patient;
        this.occupied = true;
        this.lastOccupiedStartTime = currentTime;
        patient.setAssignedBedId(id);
    }
    
    public void releasePatient(double currentTime) {
        if (occupied) {
            totalOccupiedTime += (currentTime - lastOccupiedStartTime);
            currentPatient.setAssignedBedId(null);
            currentPatient = null;
            occupied = false;
        }
    }
    
    public double getUtilization(double totalTime) {
        return totalOccupiedTime / totalTime;
    }
    
    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public Patient getCurrentPatient() { return currentPatient; }
    public boolean isOccupied() { return occupied; }
}

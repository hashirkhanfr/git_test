package trial3;

public class Bed {
    private int id;
    private String type;
    private Patient currentPatient;
    private boolean occupied;
    private double totalOccupiedTime;
    private double lastOccupiedStartTime;
    
    public Bed(int id, String type) {
        this.id = id;
        this.type = type;
        this.occupied = false;
        this.totalOccupiedTime = 0;
        this.lastOccupiedStartTime = 0;
    }
    
    
    public void assignPatient(Patient patient, double currentTime) {
        this.currentPatient = patient;       // assign the variable with the new patient object.
        this.occupied = true;                    // set the occupied status
        this.lastOccupiedStartTime = currentTime;    
        patient.setAssignedBedId(id);
    }
    
    public void releasePatient(double currentTime) {
        if (occupied) {
            totalOccupiedTime += (currentTime - lastOccupiedStartTime); //total occupied time required for calculations and GUI
            currentPatient.setAssignedBedId(0);                      // take the patient object it holds, and sets his bed id to null
            currentPatient = null;                                 
            occupied = false;
        }
    }
    
    //used in gui
    public double getUtilization(double totalTime) {
        return totalOccupiedTime / totalTime;
    }
    
    //getters
    public int getId() { return id; }
    public String getType() { return type; }
    public Patient getCurrentPatient() { return currentPatient; }
    public boolean isOccupied() { return occupied; }
}
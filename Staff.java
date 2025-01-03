package COPY;

import java.util.ArrayList;

public class Staff {
    private int id;
    private String role;
    private int maxPatients;
    private ArrayList<Patient> assignedPatients;
    private double totalWorkTime;
    private double lastWorkStartTime;
    
    public Staff(int id, String role, int maxPatients) {
        this.id = id;
        this.role = role;
        this.maxPatients = maxPatients;
        this.assignedPatients = new ArrayList<>();
        this.totalWorkTime = 0;
        this.lastWorkStartTime = 0;
    }
    
    public boolean canTakePatient() {
        return assignedPatients.size() < maxPatients;
    }
    
    public void assignPatient(Patient patient, double currentTime) {
        if (assignedPatients.size() < maxPatients) {
            if (assignedPatients.isEmpty()) {
                lastWorkStartTime = currentTime;
            }
            assignedPatients.add(patient);
        }
    }
    
    public void removePatientFromStaff(Patient patient, double currentTime) {
        assignedPatients.remove(patient);
        if (assignedPatients.isEmpty()) {
            totalWorkTime += (currentTime - lastWorkStartTime);
        }
    }

    
    public double getUtilization(double totalTime) {
        return totalWorkTime / totalTime;
    }
    
    public int getId() { return id; }
    public String getRole() { return role; }
    public int getPatientCount() { return assignedPatients.size(); }
    public ArrayList<Patient> getAssignedPatients() { return assignedPatients; }

	public int getMaxPatients() {
		return maxPatients;
	}
}
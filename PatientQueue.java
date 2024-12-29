package trial3;

import java.util.ArrayList;

public class PatientQueue {
    private ArrayList<Patient> patients;
    private final int priority;
    
    public PatientQueue(int priority) {
        this.priority = priority;
        this.patients = new ArrayList<>();
    }
    
    public void enqueue(Patient patient) {
        patients.add(patient);
    }
    
    public Patient dequeue() {
        if (patients.isEmpty()) {
            return null;
        }
        return patients.remove(0);
    }
    
    public Patient peek() {
        return patients.isEmpty() ? null : patients.get(0);
    }
    
    public boolean isEmpty() { return patients.isEmpty(); }
    public int getSize() { return patients.size(); }
    public int getPriority() { return priority; }
}
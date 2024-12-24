package trial1;

import java.io.*;
import java.util.Random;


// --- Bed Class ---
public class Bed {
    private String type;
    private boolean isOccupied;
    private Patient patient;

    public Bed(String type) {
        this.type = type;
        this.isOccupied = false;
        this.patient = null;
    }

    public boolean isOccupied() { return isOccupied; }
    public String getType() { return type; }
    public Patient getPatient() { return patient; }

    public void assignPatient(Patient patient) {
        this.patient = patient;
        this.isOccupied = true;
    }

    public void releasePatient() {
        this.patient = null;
        this.isOccupied = false;
    }
}
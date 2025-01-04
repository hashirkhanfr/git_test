package COPY;

import java.io.*;
import java.util.ArrayList;

public class CSVWriter {
    private String filename;
    public CSVWriter(String filename) {
        this.filename = filename;
        
        
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("Patient ID,Category,Disease,Waiting Time,Treatment Time,Discharge Time,Bed Allotted,Handled By");
            fw.write("\n");
        } catch (IOException e) {
            System.err.println("!! Error creating CSV file: " + e.getMessage());
        }
    }


    public void writePatientData(Patient patient, ArrayList<Staff> assignedStaff) {
        try (FileWriter fw = new FileWriter(filename, true)) { // true for append mode
            String line = "";

            line += patient.getId() + ",";
            line += patient.getCategory() + ",";
            line += patient.getDisease() + ",";
            line += String.format("%.2f", patient.getWaitTime() / 60) + ","; //waiting time in minutes
            line += String.format("%.2f", patient.getTreatmentTime() / 60) + ",";
            line += String.format("%.2f", patient.getDischargeTime() / 60) + ",";
            line += "BED-" + patient.getAssignedBedId()  + ",";

            // Handled By (Staff)
            if (assignedStaff != null && !assignedStaff.isEmpty()) {
                String staffInfo = "";
                for (int i = 0; i < assignedStaff.size(); i++) {
                    if (i > 0) staffInfo += ";";
                    Staff staff = assignedStaff.get(i);
                    staffInfo += "STAFF-"+ staff.getId()+ "(" + staff.getRole() + ")";
                }
                line += staffInfo;
            } else {
                line += "None";
            }

            // Write the line to file
            fw.write(line);
            fw.write("\n");

        } catch (IOException e) {
            System.err.println(" !! Error writing to CSV file: " + e.getMessage());
        }
    }


}
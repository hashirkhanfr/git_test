package trial1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HospitalEDGui {
    int patientID = 0;
    private JFrame frame;
    private JTextArea resultArea;
    private JTextField simulationDurationField;
    private JTextField resuscitationBedsField, acuteBedsField, subacuteBedsField, minorOpBedsField;
    private JTextField consultantsField, registrarsField, seniorResidentsField, juniorResidentsField, internsField;
    private JButton startSimulationButton, stopSimulationButton;
    private JProgressBar progressBar;
    private boolean stopSimulationFlag = false; // Flag to stop the simulation

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                HospitalEDGui window = new HospitalEDGui();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public HospitalEDGui() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 700, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblSimulationDuration = new JLabel("Simulation Duration:");
        lblSimulationDuration.setBounds(10, 11, 150, 14);
        frame.getContentPane().add(lblSimulationDuration);

        simulationDurationField = new JTextField();
        simulationDurationField.setBounds(160, 8, 100, 20);
        frame.getContentPane().add(simulationDurationField);
        simulationDurationField.setColumns(10);

        // Other input fields for hospital parameters
        createHospitalInputField("Resuscitation Beds", 40, 40);
        createHospitalInputField("Acute Beds", 40, 70);
        createHospitalInputField("Subacute Beds", 40, 100);
        createHospitalInputField("Minor Op Beds", 40, 130);
        createHospitalInputField("Consultants", 40, 160);
        createHospitalInputField("Registrars", 40, 190);
        createHospitalInputField("Senior Residents", 40, 220);
        createHospitalInputField("Junior Residents", 40, 250);
        createHospitalInputField("Interns", 40, 280);

        // Start simulation button
        startSimulationButton = new JButton("Start Simulation");
        startSimulationButton.setBounds(160, 320, 150, 30);
        frame.getContentPane().add(startSimulationButton);

        // Stop simulation button
        stopSimulationButton = new JButton("Stop Simulation");
        stopSimulationButton.setBounds(320, 320, 150, 30);
        stopSimulationButton.setEnabled(false); // Initially disabled
        frame.getContentPane().add(stopSimulationButton);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(160, 360, 500, 20);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        frame.getContentPane().add(progressBar);

        // Result output area
        resultArea = new JTextArea();
        resultArea.setBounds(160, 390, 500, 100);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBounds(160, 390, 500, 100);
        frame.getContentPane().add(scrollPane);

        // Start Simulation Button ActionListener
        startSimulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get simulation duration and hospital parameters
                    double simulationDuration = Double.parseDouble(simulationDurationField.getText());
                    int resuscitationBeds = Integer.parseInt(resuscitationBedsField.getText());
                    int acuteBeds = Integer.parseInt(acuteBedsField.getText());
                    int subacuteBeds = Integer.parseInt(subacuteBedsField.getText());
                    int minorOpBeds = Integer.parseInt(minorOpBedsField.getText());
                    int consultants = Integer.parseInt(consultantsField.getText());
                    int registrars = Integer.parseInt(registrarsField.getText());
                    int seniorResidents = Integer.parseInt(seniorResidentsField.getText());
                    int juniorResidents = Integer.parseInt(juniorResidentsField.getText());
                    int interns = Integer.parseInt(internsField.getText());

                    // Create the Hospital object
                    Hospital hospital = new Hospital(simulationDuration, resuscitationBeds, acuteBeds, subacuteBeds, minorOpBeds,
                            consultants, registrars, seniorResidents, juniorResidents, interns);

                    // Run simulation in a separate thread
                    stopSimulationFlag = false;
                    startSimulationButton.setEnabled(false);
                    stopSimulationButton.setEnabled(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runSimulation(hospital);
                        }
                    }).start();

                } catch (NumberFormatException ex) {
                    resultArea.setText("Error: Please enter valid numeric values for all fields.");
                } catch (Exception ex) {
                    resultArea.setText("An error occurred: " + ex.getMessage());
                }
            }
        });

        // Stop Simulation Button ActionListener
        stopSimulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulationFlag = true; // Set the flag to stop the simulation
                stopSimulationButton.setEnabled(false); // Disable the stop button after clicking
            }
        });
    }

    // Helper method to create input fields for hospital parameters
    private void createHospitalInputField(String labelText, int x, int y) {
        JLabel label = new JLabel(labelText);
        label.setBounds(x, y, 150, 14);
        frame.getContentPane().add(label);

        JTextField textField = new JTextField();
        textField.setBounds(160, y, 100, 20);
        frame.getContentPane().add(textField);

        switch (labelText) {
            case "Resuscitation Beds":
                resuscitationBedsField = textField;
                break;
            case "Acute Beds":
                acuteBedsField = textField;
                break;
            case "Subacute Beds":
                subacuteBedsField = textField;
                break;
            case "Minor Op Beds":
                minorOpBedsField = textField;
                break;
            case "Consultants":
                consultantsField = textField;
                break;
            case "Registrars":
                registrarsField = textField;
                break;
            case "Senior Residents":
                seniorResidentsField = textField;
                break;
            case "Junior Residents":
                juniorResidentsField = textField;
                break;
            case "Interns":
                internsField = textField;
                break;
        }
    }

    // Method to run the simulation and update the GUI
    private void runSimulation(Hospital hospital) {
        double currentTime = 0;
        double simulationDuration = hospital.getSimulationDuration();

        while (currentTime < simulationDuration && !stopSimulationFlag) {
            // 1. Generate a new patient
            if (Math.random() < 0.1) { // Example probability of patient arrival
                int category = (int) (Math.random() * 5) + 1;
                double arrivalTime = currentTime;
                double treatmentTime = 15 + Math.random() * 45;
                Patient patient = new Patient(patientID++, arrivalTime, 0, treatmentTime, 0, category);
                hospital.addPatientToQueue(patient);

                // Update the GUI with patient arrival details
                updateResultArea("New patient arrived: " + patient);
            }

            // 2. Assign patients to beds and treat them
            hospital.assignPatientsToBeds();
            hospital.treatPatients();

            // 3. Update time and display simulation progress
            currentTime += 1;
            int progress = (int) ((currentTime / simulationDuration) * 100);
            updateProgressBar(progress);

            // 4. Update the GUI with current simulation time
            updateResultArea("Simulation Time: " + currentTime + " seconds");

            // Sleep for a short period to slow down the simulation for visibility
            try {
                Thread.sleep(500);  // Adjust as needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Simulation finished or stopped
        if (stopSimulationFlag) {
            updateResultArea("Simulation was stopped.");
        } else {
            updateResultArea("Simulation completed successfully.");
        }

        // Enable the start button again after completion or stop
        startSimulationButton.setEnabled(true);
        stopSimulationButton.setEnabled(false);
    }

    // Helper method to update the result area in the GUI
    private void updateResultArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                resultArea.append(text + "\n");
            }
        });
    }

    // Helper method to update the progress bar
    private void updateProgressBar(final int progress) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(progress);
            }
        });
    }
}

package trial3;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class HospitalSimulationGUI {
    // Existing fields
    private JFrame frame;
    private JTextField simulationDurationField;
    private JTextField bedCountField;
    private JTextField staffCountField;
    private JTextField patientArrivalField;
    private JTextField categoryAssignmentField;
    private JTextField staffAssignmentField;
    private JTextField bedAssignmentField;
    private JTextArea bedStatusArea;
    private JTextField bedSummaryField;
    private JTextArea staffStatusArea;
    private JTextField staffSummaryField;
    private JTextArea resultArea;
    private JButton startSimulationButton;
    private JButton stopSimulationButton;
    private JProgressBar progressBar;
    

    // New fields for patient status
    private JTextArea patientStatusArea;
    private JTextField patientSummaryField;

    private volatile boolean stopSimulationFlag;
    private Hospital hospital;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                HospitalSimulationGUI window = new HospitalSimulationGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public HospitalSimulationGUI() {
        initialize();
    }

    private void initialize() {
        // Create frame to be maximized
    	frame = new JFrame("Hospital ED Simulation");
    	frame.setSize(1024, 768); // Fixed size for WindowBuilder compatibility
    	frame.setLocationRelativeTo(null); // Center frame on screen
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().setLayout(null);
    	
    	JPanel headerPanel = new JPanel();
        headerPanel.setBounds(10, 10, 1366, 34);
        headerPanel.setBackground(new Color(70, 130, 180)); // Steel Blue
        JLabel titleLabel = new JLabel("Hospital Emergency Department Simulation", JLabel.CENTER);
        titleLabel.setFont(new Font("Gotham Black", Font.PLAIN, 27));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        frame.getContentPane().add(headerPanel);

        // Input Parameters Section (Top Left)
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(BorderFactory.createTitledBorder("Simulation Parameters"));
        inputPanel.setBounds(20, 87, 350, 120);
        inputPanel.setLayout(null);
        frame.getContentPane().add(inputPanel);

        // Add input components
        JLabel durationLabel = new JLabel("Simulation Duration (minutes):");
        durationLabel.setBounds(10, 20, 200, 25);
        inputPanel.add(durationLabel);

        simulationDurationField = new JTextField("1");
        simulationDurationField.setBounds(210, 20, 100, 25);
        inputPanel.add(simulationDurationField);

        JLabel bedCountLabel = new JLabel("Total Beds:");
        bedCountLabel.setBounds(10, 50, 200, 25);
        inputPanel.add(bedCountLabel);

        bedCountField = new JTextField("8");
        bedCountField.setBounds(210, 50, 100, 25);
        inputPanel.add(bedCountField);

        JLabel staffCountLabel = new JLabel("Total Staff:");
        staffCountLabel.setBounds(10, 80, 200, 25);
        inputPanel.add(staffCountLabel);

        staffCountField = new JTextField("9");
        staffCountField.setBounds(210, 80, 100, 25);
        inputPanel.add(staffCountField);

        // Controls Section (Top Center)
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setBounds(380, 87, 292, 120);
        controlPanel.setLayout(null);
        frame.getContentPane().add(controlPanel);

        startSimulationButton = new JButton("Start Simulation");
        startSimulationButton.setBackground(new Color(51, 204, 102));
        startSimulationButton.setBounds(10, 20, 130, 30);
        controlPanel.add(startSimulationButton);

        stopSimulationButton = new JButton("Stop Simulation");
        stopSimulationButton.setForeground(new Color(255, 255, 255));
        stopSimulationButton.setBackground(new Color(255, 0, 51));
        stopSimulationButton.setBounds(150, 20, 130, 30);
        stopSimulationButton.setEnabled(false);
        controlPanel.add(stopSimulationButton);

        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(new Color(0, 204, 102));
        progressBar.setBounds(10, 60, 270, 30);
        progressBar.setStringPainted(true);
        controlPanel.add(progressBar);

        // Patient Updates Section (Middle Left)
        JPanel updatesPanel = new JPanel();
        updatesPanel.setBorder(BorderFactory.createTitledBorder("Patient Updates"));
        updatesPanel.setBounds(20, 217, 652, 200);
        updatesPanel.setLayout(null);
        frame.getContentPane().add(updatesPanel);

        // Add update fields
        JLabel patientArrivalLabel = new JLabel("Latest Arrival:");
        patientArrivalLabel.setBounds(10, 20, 150, 25);
        updatesPanel.add(patientArrivalLabel);

        patientArrivalField = new JTextField();
        patientArrivalField.setBounds(170, 20, 472, 25);
        patientArrivalField.setEditable(false);
        updatesPanel.add(patientArrivalField);

        JLabel categoryAssignmentLabel = new JLabel("Assigned Category:");
        categoryAssignmentLabel.setBounds(10, 60, 150, 25);
        updatesPanel.add(categoryAssignmentLabel);

        categoryAssignmentField = new JTextField();
        categoryAssignmentField.setBounds(170, 60, 472, 25);
        categoryAssignmentField.setEditable(false);
        updatesPanel.add(categoryAssignmentField);

        JLabel staffAssignmentLabel = new JLabel("Assigned Staff:");
        staffAssignmentLabel.setBounds(10, 100, 150, 25);
        updatesPanel.add(staffAssignmentLabel);

        staffAssignmentField = new JTextField();
        staffAssignmentField.setBounds(170, 100, 472, 25);
        staffAssignmentField.setEditable(false);
        updatesPanel.add(staffAssignmentField);

        JLabel bedAssignmentLabel = new JLabel("Assigned Bed:");
        bedAssignmentLabel.setBounds(10, 140, 150, 25);
        updatesPanel.add(bedAssignmentLabel);

        bedAssignmentField = new JTextField();
        bedAssignmentField.setBounds(170, 140, 472, 25);
        bedAssignmentField.setEditable(false);
        updatesPanel.add(bedAssignmentField);


        // Bed Status Panel (Top Right)
        JPanel bedPanel = new JPanel();
        bedPanel.setBorder(BorderFactory.createTitledBorder("Bed Status"));
        bedPanel.setBounds(682, 87, 700, 250);
        bedPanel.setLayout(null);
        frame.getContentPane().add(bedPanel);

        bedSummaryField = new JTextField();
        bedSummaryField.setBounds(10, 20, 700 - 20, 25);
        bedSummaryField.setEditable(false);
        bedPanel.add(bedSummaryField);

        bedStatusArea = new JTextArea();
        bedStatusArea.setEditable(false);
        JScrollPane bedScrollPane = new JScrollPane(bedStatusArea);
        bedScrollPane.setBounds(10, 50, 680, 190);
        bedPanel.add(bedScrollPane);

        // Staff Status Panel (Middle Right)
        JPanel staffPanel = new JPanel();
        staffPanel.setBorder(BorderFactory.createTitledBorder("Staff Status"));
        staffPanel.setBounds(682, 342, 700, 250);
        staffPanel.setLayout(null);
        frame.getContentPane().add(staffPanel);

        staffSummaryField = new JTextField();
        staffSummaryField.setBounds(10, 20, 700 - 20, 25);
        staffSummaryField.setEditable(false);
        staffPanel.add(staffSummaryField);

        staffStatusArea = new JTextArea();
        staffStatusArea.setEditable(false);
        JScrollPane staffScrollPane = new JScrollPane(staffStatusArea);
        staffScrollPane.setBounds(10, 50, 680, 190);
        staffPanel.add(staffScrollPane);

        // Patient Status Panel (Bottom Right)
        JPanel patientPanel = new JPanel();
        patientPanel.setBorder(BorderFactory.createTitledBorder("Patient Status"));
        patientPanel.setBounds(682, 602, 700, 180);
        patientPanel.setLayout(null);
        frame.getContentPane().add(patientPanel);

        patientSummaryField = new JTextField();
        patientSummaryField.setBounds(10, 20, 700 - 20, 25);
        patientSummaryField.setEditable(false);
        patientPanel.add(patientSummaryField);

        patientStatusArea = new JTextArea();
        patientStatusArea.setBackground(new Color(255, 255, 255));
        patientStatusArea.setEditable(false);
        JScrollPane patientScrollPane = new JScrollPane(patientStatusArea);
        patientScrollPane.setBounds(10, 50, 680, 100);
        patientPanel.add(patientScrollPane);

        // Results Section (Bottom Left)
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Simulation Results"));
        resultsPanel.setBounds(20, 427, 652, 386);
        resultsPanel.setLayout(null);
        frame.getContentPane().add(resultsPanel);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBounds(10, 20, 632, 340);
        resultsPanel.add(resultScroll);

        setupActionListeners();
    }

    // Add new method to update patient status using arrays
    public void updatePatientStatus(int[] waitingPatients) {
        SwingUtilities.invokeLater(() -> {
            int totalWaiting = 0;
            StringBuilder details = new StringBuilder();
            
            // Calculate total and build status text
            for (int i = 0; i < waitingPatients.length; i++) {
                totalWaiting += waitingPatients[i];
                details.append(String.format("Category %d: %d waiting\n", i + 1, waitingPatients[i]));
            }

            patientSummaryField.setText(String.format("Total Waiting Patients: %d", totalWaiting));
            patientStatusArea.setText(details.toString());
        });
    }

    // Existing methods remain the same
    private void setupActionListeners() {
        startSimulationButton.addActionListener(e -> {
            try {
                double duration = Double.parseDouble(simulationDurationField.getText());
                int bedCount = Integer.parseInt(bedCountField.getText());
                int staffCount = Integer.parseInt(staffCountField.getText());

                startSimulation(duration, bedCount, staffCount);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                    "Please enter valid numbers for all fields.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        stopSimulationButton.addActionListener(e -> {
            stopSimulationFlag = true;
            stopSimulationButton.setEnabled(false);
        });
    }

    // Update methods for bed and staff status
    public void updateBedStatus(ArrayList<Bed> beds) {
        SwingUtilities.invokeLater(() -> {
            int totalBeds = beds.size();
            int occupiedBeds = 0;
            StringBuilder details = new StringBuilder();

            for (Bed bed : beds) {
                if (bed.isOccupied()) {
                    occupiedBeds++;
                }
                details.append(String.format("BED-%d (%s): %s\n", 
                    bed.getId(), 
                    bed.getType(),
                    bed.isOccupied() ? "Occupied by Patient " + bed.getCurrentPatient().getId() : "Available"));
            }

            bedSummaryField.setText(String.format("Occupied Beds: %d/%d", occupiedBeds, totalBeds));
            bedStatusArea.setText(details.toString());
        });
    }

    public void updateStaffStatus(ArrayList<Staff> staffMembers) {
        SwingUtilities.invokeLater(() -> {
            int totalStaff = staffMembers.size();
            int occupiedStaff = 0;
            StringBuilder details = new StringBuilder();

            for (Staff staff : staffMembers) {
                int patientCount = staff.getPatientCount();
                if (patientCount > 0) {
                    occupiedStaff++;
                }
                details.append(String.format("%s (%s): %d/%d patients\n", 
                    staff.getId(), 
                    staff.getRole(), 
                    patientCount,
                    staff.getMaxPatients()));
            }

            staffSummaryField.setText(String.format("Active Staff: %d/%d", occupiedStaff, totalStaff));
            staffStatusArea.setText(details.toString());
        });
    }

    private void startSimulation(double duration, int bedCount, int staffCount) {
        stopSimulationFlag = false;
        stopSimulationButton.setEnabled(true);
        progressBar.setValue(0);
        resultArea.setText("");
        bedStatusArea.setText("");
        staffStatusArea.setText("");
        bedSummaryField.setText("");
        staffSummaryField.setText("");

        patientArrivalField.setText("");
        categoryAssignmentField.setText("");
        staffAssignmentField.setText("");
        bedAssignmentField.setText("");

        new Thread(() -> {
            try {
                hospital = new Hospital(duration, bedCount, staffCount, this);

                double totalSteps = duration * 60; // Convert minutes to seconds
                double completedSteps = 0;

                while (!stopSimulationFlag && completedSteps < totalSteps) {
                    hospital.threadStep();
                    completedSteps++;
                    double progress = (completedSteps / totalSteps) * 100;

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue((int) progress);
                    });

                    Thread.sleep(100); // Simulate 1-second steps
                }

                SwingUtilities.invokeLater(() -> {
                    resultArea.setText("Simulation completed.\n\n" + hospital.generateReport());
                    stopSimulationButton.setEnabled(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    resultArea.setText("Error during simulation: " + ex.getMessage());
                    startSimulationButton.setEnabled(false);
                    stopSimulationButton.setEnabled(false);
                });
            }
        }).start();
    }

    public void updatePatientArrival(int patientId) {
        SwingUtilities.invokeLater(() -> {
            patientArrivalField.setText("Patient " + patientId + " arrived at time " + 
                String.format("%.1f", hospital.getCurrentTime()));
        });
    }

    public void updateCategoryAssignment(int patientId, int category) {
        SwingUtilities.invokeLater(() -> {
            categoryAssignmentField.setText("Patient " + patientId + " assigned to Category " + 
                category + " at time " + String.format("%.1f", hospital.getCurrentTime()));
        });
    }

    public void updateStaffAssignment(int patientId, String staffId, String role) {
        SwingUtilities.invokeLater(() -> {
            staffAssignmentField.setText("Patient " + patientId + " assigned to " + role + 
                " (" + staffId + ") at time " + String.format("%.1f", hospital.getCurrentTime()));
        });
    }

    public void updateBedAssignment(int patientId, int bedId) {
        SwingUtilities.invokeLater(() -> {
            bedAssignmentField.setText("Patient " + patientId + " assigned to BED-" + bedId + 
                " at time " + String.format("%.1f", hospital.getCurrentTime()));
        });
    }
}
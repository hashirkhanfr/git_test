package trial2claude;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.TitledBorder;

public class HospitalSimulationGUI {
    private JFrame frame;
    private JTextArea resultArea;
    private JTextArea currentStateArea;
    private JTextField simulationDurationField;
    private JTextField bedCountField;
    private JTextField staffCountField;
    private JButton startSimulationButton;
    private JButton stopSimulationButton;
    private JProgressBar progressBar;
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
        frame = new JFrame("Hospital ED Simulation");
        frame.setBounds(100, 100, 1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        frame.getContentPane().add(mainPanel);
        GridBagConstraints gbc = new GridBagConstraints();

        // Input Panel
        JPanel inputPanel = createInputPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(inputPanel, gbc);

        // Control Panel (Start/Stop buttons and Progress Bar)
        JPanel controlPanel = createControlPanel();
        gbc.gridy = 1;
        mainPanel.add(controlPanel, gbc);

        // Output Panel (Results and Current State)
        JPanel outputPanel = createOutputPanel();
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(outputPanel, gbc);

        setupActionListeners();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Simulation Parameters",
            TitledBorder.LEFT, TitledBorder.TOP));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Simulation Duration
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Simulation Duration (minutes):"), gbc);

        simulationDurationField = new JTextField("1440", 10); // 24 hours default
        gbc.gridx = 1;
        panel.add(simulationDurationField, gbc);

        // Bed Count
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Total Beds:"), gbc);

        bedCountField = new JTextField("20", 10);
        gbc.gridx = 1;
        panel.add(bedCountField, gbc);

        // Staff Count
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Total Staff:"), gbc);

        staffCountField = new JTextField("15", 10);
        gbc.gridx = 1;
        panel.add(staffCountField, gbc);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        startSimulationButton = new JButton("Start Simulation");
        stopSimulationButton = new JButton("Stop Simulation");
        stopSimulationButton.setEnabled(false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 25));

        panel.add(startSimulationButton);
        panel.add(stopSimulationButton);
        panel.add(progressBar);

        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Current State Area
        currentStateArea = new JTextArea();
        currentStateArea.setEditable(false);
        JScrollPane currentStateScroll = new JScrollPane(currentStateArea);
        currentStateScroll.setBorder(BorderFactory.createTitledBorder("Current State"));

        // Result Area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Simulation Results"));

        panel.add(currentStateScroll);
        panel.add(resultScroll);

        return panel;
    }

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

    private void startSimulation(double duration, int bedCount, int staffCount) {
        // Reset UI state
        stopSimulationFlag = false;
        startSimulationButton.setEnabled(false);
        stopSimulationButton.setEnabled(true);
        progressBar.setValue(0);
        resultArea.setText("");
        currentStateArea.setText("");

        // Create and run simulation in background thread
        new Thread(() -> {
            try {
                hospital = new Hospital(duration, bedCount, staffCount);

                // Monitoring thread for updating the UI
                Thread monitorThread = new Thread(() -> {
                    while (!stopSimulationFlag) {
                        try {
                            SwingUtilities.invokeLater(() -> {
                                currentStateArea.setText(hospital.getCurrentState());
                            });
                            Thread.sleep(1000); // Update every second
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                });
                monitorThread.start();

                // Run the simulation with progress updates
                double startTime = System.currentTimeMillis();
                while (true) {
                    hospital.step(); // Fix: Simulate one step

                    // Update progress
                    double progress = (System.currentTimeMillis() - startTime) / (duration * 60 * 1000) * 100;
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue((int) Math.min(progress, 100));
                    });

                    if (stopSimulationFlag || progress >= 100) {
                        break;
                    }

                    Thread.sleep(50); // Control simulation speed
                }

                // Show final results
                SwingUtilities.invokeLater(() -> {
                    resultArea.setText("Simulation completed.\n\n" + hospital.generateReport());
                    startSimulationButton.setEnabled(true);
                    stopSimulationButton.setEnabled(false);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    resultArea.setText("Error during simulation: " + ex.getMessage());
                    startSimulationButton.setEnabled(true);
                    stopSimulationButton.setEnabled(false);
                });
            }
        }).start();
    }
}

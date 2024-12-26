package trial2claude;

public class Hospital {
    private static final int MAX_QUEUES = 5;
    private static final int QUEUE_CAPACITY = 100;
    
    private PatientQueue[] queues;
    private Bed[] beds;
    private Staff[] staff;
    private SimulationMetrics metrics;
    private double currentTime;
    private double simulationDuration;
    private int nextPatientId;
    
    public Hospital(double simulationDuration, int bedCount, int staffCount) {
        this.simulationDuration = simulationDuration;
        this.currentTime = 0;
        this.nextPatientId = 1;
        
        // Initialize queues
        this.queues = new PatientQueue[MAX_QUEUES];
        for (int i = 0; i < MAX_QUEUES; i++) {
            queues[i] = new PatientQueue(i + 1, QUEUE_CAPACITY);
        }
        
        // Initialize beds
        this.beds = new Bed[bedCount];
        for (int i = 0; i < bedCount; i++) {
            String type = i < bedCount/4 ? "Resuscitation" :
                         i < bedCount/2 ? "Acute" :
                         i < 3*bedCount/4 ? "Subacute" : "Minor";
            beds[i] = new Bed("BED_" + (i+1), type);
        }
        
        // Initialize staff
        this.staff = new Staff[staffCount];
        for (int i = 0; i < staffCount; i++) {
            String role = i < staffCount/5 ? "Consultant" :
                         i < 2*staffCount/5 ? "Registrar" :
                         i < 3*staffCount/5 ? "Senior Resident" :
                         i < 4*staffCount/5 ? "Junior Resident" : "Intern";
            int maxPatients = role.equals("Consultant") ? 2 :
                            role.equals("Registrar") ? 3 :
                            role.equals("Senior Resident") ? 4 :
                            role.equals("Junior Resident") ? 4 : 5;
            staff[i] = new Staff("STAFF_" + (i+1), role, maxPatients);
        }
        
        this.metrics = new SimulationMetrics();
    }
    
    public void runSimulation() {
        while (currentTime < simulationDuration) {
            // Generate new patients
            if (shouldGenerateNewPatient()) {
                generatePatient();
            }
            
            // Process queues and assign beds
            processQueues();
            
            // Update patient treatment progress
            updateTreatments();
            
            // Advance time
            currentTime += 1;
        }
        
        // Generate final report
        System.out.println(metrics.generateReport());
    }
    
    private boolean shouldGenerateNewPatient() {
        // Use Weibull distribution to determine if new patient should arrive
        return Math.random() < Probability.weibull(currentTime % 60, 30, 2);
    }
    
    private void generatePatient() {
        // Generate category based on empirical distribution
        int category = generatePatientCategory();
        Patient patient = new Patient(nextPatientId++, category, currentTime);
        
        // Add appropriate diagnostic studies
        if (category <= 3) {
            patient.addDiagnosticStudy("Blood Work", 
                Probability.generateExponentialRandom(30)); // 30 min mean duration
            if (category <= 2) {
                patient.addDiagnosticStudy("Imaging", 
                    Probability.generateExponentialRandom(45)); // 45 min mean duration
            }
        }
        
        // Add to appropriate queue
        queues[category - 1].enqueue(patient);
    }
    
    private int generatePatientCategory() {
        double rand = Math.random();
        if (rand < 0.05) return 1;      // 5% category 1 (most urgent)
        if (rand < 0.20) return 2;      // 15% category 2
        if (rand < 0.50) return 3;      // 30% category 3
        if (rand < 0.80) return 4;      // 30% category 4
        return 5;                       // 20% category 5 (least urgent)
    }
    
    private void processQueues() {
        // Process queues in priority order (1 to 5)
        for (int i = 0; i < MAX_QUEUES; i++) {
            while (!queues[i].isEmpty()) {
                Patient patient = queues[i].peek();
                
                // Try to assign the patient to a bed
                if (assignPatientToBed(patient)) {
                    queues[i].dequeue(); // Remove from queue only if successfully assigned
                    patient.setWaitTime(currentTime - patient.getArrivalTime());
                    metrics.recordPatient(patient);
                } else {
                    // If we couldn't assign this patient, we won't be able to assign any others in this queue
                    break;
                }
            }
        }
    }
    
    private boolean assignPatientToBed(Patient patient) {
        // First try to find an appropriate empty bed
        Bed assignedBed = findEmptyBed(patient.getCategory());
        
        if (assignedBed == null && patient.getCategory() <= 2) {
            // For high priority patients, try preemption
            assignedBed = findPreemptableBed(patient.getCategory());
            if (assignedBed != null) {
                // Preempt current patient
                Patient preemptedPatient = assignedBed.getCurrentPatient();
                preemptedPatient.setPreempted(true);
                queues[preemptedPatient.getCategory() - 1].enqueue(preemptedPatient);
                releasePatient(assignedBed);
                metrics.recordPreemption();
            }
        }
        
        if (assignedBed != null) {
            // Assign patient to bed and find available staff
            assignedBed.assignPatient(patient, currentTime);
            assignStaffToPatient(patient);
            return true;
        }
        
        return false;
    }
    
    private Bed findEmptyBed(int category) {
        String requiredType = getBedTypeForCategory(category);
        for (Bed bed : beds) {
            if (!bed.isOccupied() && bed.getType().equals(requiredType)) {
                return bed;
            }
        }
        return null;
    }
    
    private Bed findPreemptableBed(int category) {
        String requiredType = getBedTypeForCategory(category);
        Bed bestBed = null;
        int highestPreemptableCategory = 5;
        
        for (Bed bed : beds) {
            if (bed.isOccupied() && bed.getType().equals(requiredType)) {
                Patient currentPatient = bed.getCurrentPatient();
                if (currentPatient.getCategory() > category + 1 && 
                    currentPatient.getCategory() < highestPreemptableCategory) {
                    bestBed = bed;
                    highestPreemptableCategory = currentPatient.getCategory();
                }
            }
        }
        
        return bestBed;
    }
    
    private String getBedTypeForCategory(int category) {
        switch (category) {
            case 1: return "Resuscitation";
            case 2: return "Acute";
            case 3: return "Subacute";
            default: return "Minor";
        }
    }
    
    private void assignStaffToPatient(Patient patient) {
        // Assign appropriate staff based on patient category
        if (patient.getCategory() <= 2) {
            assignStaffByRole(patient, "Consultant");
            assignStaffByRole(patient, "Registrar");
        } else if (patient.getCategory() == 3) {
            assignStaffByRole(patient, "Senior Resident");
            assignStaffByRole(patient, "Junior Resident");
        } else {
            assignStaffByRole(patient, "Junior Resident");
            assignStaffByRole(patient, "Intern");
        }
    }
    
    private void assignStaffByRole(Patient patient, String role) {
        for (Staff member : staff) {
            if (member.getRole().equals(role) && member.canTakePatient()) {
                member.assignPatient(patient, currentTime);
                break;
            }
        }
    }
    
    private void updateTreatments() {
        for (Bed bed : beds) {
            if (bed.isOccupied()) {
                Patient patient = bed.getCurrentPatient();
                if (currentTime - patient.getArrivalTime() - patient.getWaitTime() >= patient.getTreatmentTime()) {
                    // Treatment complete, release patient
                    releasePatient(bed);
                }
            }
        }
    }
    
    private void releasePatient(Bed bed) {
        Patient patient = bed.getCurrentPatient();
        patient.setDischargeTime(currentTime);
        
        // Release staff assigned to this patient
        for (Staff member : staff) {
            member.releasePatient(patient, currentTime);
        }
        
        // Release bed
        bed.releasePatient(currentTime);
    }
    
    // Getters for metrics and current state
    public String getCurrentState() {
        StringBuilder state = new StringBuilder();
        state.append("Current Time: ").append(currentTime).append("\n");
        
        // Queue states
        state.append("\nQueue Status:\n");
        for (int i = 0; i < MAX_QUEUES; i++) {
            state.append(String.format("Category %d: %d patients waiting\n", 
                i + 1, queues[i].getSize()));
        }
        
        // Bed utilization
        state.append("\nBed Utilization:\n");
        int occupied = 0;
        for (Bed bed : beds) {
            if (bed.isOccupied()) occupied++;
        }
        state.append(String.format("Occupied Beds: %d/%d\n", occupied, beds.length));
        
        return state.toString();
    }
    
    public String generateReport() {
        return metrics.generateReport();
    }
    
    public void step() {
        currentTime++;

        // Generate new patient arrivals
        if (Math.random() < 0.1) { // Adjust arrival probability
            int category = (int) (Math.random() * 5) + 1; // Random category 1-5
            Patient newPatient = new Patient((int) currentTime, category, currentTime);
            queues[category - 1].enqueue(newPatient);
        }

        // Process patients in queues
        for (int i = 0; i < queues.length; i++) {
            PatientQueue queue = queues[i];
            if (!queue.isEmpty()) {
                Patient patient = queue.peek();

                // Assign staff if available
                for (Staff s : staff) {
                    if (s.canTakePatient()) {
                        s.assignPatient(patient, currentTime);
                        queue.dequeue();
                        break;
                    }
                }
            }
        }

        // Update staff work and release patients
        for (Staff s : staff) {
            for (int i = 0; i < s.getPatientCount(); i++) {
                Patient patient = s.assignedPatients[i];
                if (currentTime >= patient.getArrivalTime() + patient.getTreatmentTime()) {
                    metrics.recordPatient(patient);
                    s.releasePatient(patient, currentTime);
                }
            }
        }
    }
}

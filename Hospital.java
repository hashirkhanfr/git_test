package trial3;

import java.util.ArrayList;

public class Hospital {
    private final int MAX_QUEUES = 5;
    
    private ArrayList<PatientQueue> queues;
    private ArrayList<Bed> beds;
    private ArrayList<Staff> staff;
    private SimulationMetrics metrics;
    private double currentTime;
    private double simulationDuration;
    private int nextPatientId;
  
    private double nextArrivalTime;
    
	private HospitalSimulationGUI gui;
    private static final double INTERARRIVAL_SCALER = 10.0;
    
    public Hospital(double simulationDuration, int bedCount, int staffCount, HospitalSimulationGUI gui) {
        this.simulationDuration = simulationDuration;
        this.currentTime = 0;
        this.nextPatientId = 1;
        this.gui = gui;  // Store GUI reference
        
        // Initialize queues
        this.queues = new ArrayList<>();
        for (int i = 0; i < MAX_QUEUES; i++) {
            queues.add(new PatientQueue(i + 1));
        }
        
        // Initialize beds
        this.beds = new ArrayList<>();
        for (int i = 0; i < bedCount; i++) {
            String type = i < bedCount/4 ? "Resuscitation" :
                         i < bedCount/2 ? "Acute" :
                         i < 3*bedCount/4 ? "Subacute" : "Minor";
            beds.add(new Bed(i+1, type));
        }
        
        // Initialize staff
        this.staff = new ArrayList<>();
        for (int i = 0; i < staffCount; i++) {
        	String role;
        	if (i < staffCount / 5)            role = "Consultant";
        	else if (i < 2 * staffCount / 5)   role = "Registrar";
        	else if (i < 3 * staffCount / 5)   role = "Senior Resident";
        	else if (i < 4 * staffCount / 5)   role = "Junior Resident";
        	else                               role = "Intern";
        	int maxPatients;
        	if (role.equals("Consultant"))             maxPatients = 2;
        	else if (role.equals("Registrar"))         maxPatients = 3;
        	else if (role.equals("Senior Resident"))   maxPatients = 4;
        	else if (role.equals("Junior Resident"))   maxPatients = 4;
        	else                                       maxPatients = 5;
        	
            staff.add(new Staff("STAFF-" + (i+1), role, maxPatients));
        }
        
        this.metrics = new SimulationMetrics(beds, staff);
    }
    
    public void runSimulation() {
        while (currentTime < simulationDuration) {
            // Check if it's time for a new patient arrival
            if (currentTime >= nextArrivalTime) {
                generatePatient();
                
                // Schedule next arrival using Weibull distribution
                double interarrivalTime = Probability.generateInterarrivalTime(currentTime) * INTERARRIVAL_SCALER;
                // Ensure minimum gap of 1 second between arrivals
                nextArrivalTime = currentTime + Math.max(1.0, interarrivalTime);
            }
            
            processQueues();
            updateTreatments();
            currentTime += 1;
        }
        
        System.out.println(generateReport());
    }
    
    private void generatePatient() {
        int category = generatePatientCategory();
        Patient patient = new Patient(nextPatientId, category, currentTime);
        
        // Update GUI with new patient arrival
        if (gui != null) {
            gui.updatePatientArrival(nextPatientId);
            gui.updateCategoryAssignment(nextPatientId, category);
        }
        
        // Add diagnostic studies based on severity
        if (category <= 3) {
            double bloodworkTime = Probability.generatePostTreatmentTime(currentTime) * 0.2;
            patient.addDiagnosticStudy("Blood Work", bloodworkTime);
            
            if (category <= 2) {
                double imagingTime = Probability.generatePostTreatmentTime(currentTime) * 0.3;
                patient.addDiagnosticStudy("Imaging", imagingTime);
            }
        }
        
        queues.get(category - 1).enqueue(patient);
        nextPatientId++;
    }
    
    private int generatePatientCategory() {
        double rand = Math.random();
        // Distribution based on typical ED triage patterns
        if (rand < 0.05) return 1;      // 5% critical cases
        if (rand < 0.20) return 2;      // 15% acute cases
        if (rand < 0.50) return 3;      // 30% subacute cases
        if (rand < 0.80) return 4;      // 30% minor cases
        return 5;                        // 20% non-urgent cases
    }
    
    private void processQueues() {
        for (PatientQueue queue : queues) {
            while (!queue.isEmpty()) {
                Patient patient = queue.peek();
                
                if (assignPatientToBed(patient)) {
                    queue.dequeue();
                    patient.setWaitTime(currentTime - patient.getArrivalTime());
                    metrics.recordPatient(patient);
                }
                else {
                    break;
                }
            }
        }
    }
    
    private boolean assignPatientToBed(Patient patient) {
        // First check if we can assign appropriate staff
        int requiredStaffCount = patient.getCategory() <= 2 ? 2 : 1;
        int availableQualifiedStaff = countAvailableQualifiedStaff(patient.getCategory());
        
        if (availableQualifiedStaff < requiredStaffCount) {
            return false; // Not enough qualified staff available
        }
        
        Bed assignedBed = findEmptyBed(patient.getCategory());
        
        // Handle preemption for critical and acute patients
        if (assignedBed == null && patient.getCategory() <= 2) {
            assignedBed = findPreemptableBed(patient.getCategory());
            if (assignedBed != null) {
                Patient preemptedPatient = assignedBed.getCurrentPatient();
                preemptedPatient.setPreempted(true);
                queues.get(preemptedPatient.getCategory() - 1).enqueue(preemptedPatient);
                releasePatient(assignedBed);
                metrics.recordPreemption();
            }
        }
        
        if (assignedBed != null) {
            assignedBed.assignPatient(patient, currentTime);
            if (gui != null) {
                gui.updateBedAssignment(patient.getId(), assignedBed.getId());
            }
            assignStaffToPatient(patient);
            startDiagnosticStudies(patient);
            return true;
        }
        
        return false;
    }
    
    private int countAvailableQualifiedStaff(int category) {
        int count = 0;
        for (Staff member : staff) {
            if (member.canTakePatient() && isStaffQualifiedForCategory(member, category)) {
                count++;
            }
        }
        return count;
    }
    
    private Bed findEmptyBed(int category) {
        String requiredType = getBedTypeForCategory(category);
        for (Bed bed : beds)
            if (!bed.isOccupied() && bed.getType().equals(requiredType))
                return bed;
        
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
        int category = patient.getCategory();
        
        // For Category 1-2 patients
        if (category <= 2) {
            // Need both senior (consultant/registrar) and junior staff
            boolean seniorAssigned = assignStaffByRole(patient, new String[]{"Consultant", "Registrar"});
            boolean juniorAssigned = assignStaffByRole(patient, new String[]{"Senior Resident", "Junior Resident"});
            
            // If we couldn't get required staff, patient must wait
            if (!seniorAssigned || !juniorAssigned) {
                releaseAllAssignedStaff(patient);
                return;
            }
        }
        // For Category 3-5 patients
        else {
            // Try to assign in order of preference
            boolean staffAssigned = assignStaffByRole(patient, 
                new String[]{"Senior Resident", "Junior Resident", "Intern"});
            
            if (!staffAssigned) {
                return; // No suitable staff available
            }
        }
    }
    
    private boolean assignStaffByRole(Patient patient, String[] acceptableRoles) {
        for (String role : acceptableRoles) {
            for (Staff member : staff) {
                if (member.getRole().equals(role) && member.canTakePatient() && 
                    isStaffQualifiedForCategory(member, patient.getCategory())) {
                    member.assignPatient(patient, currentTime);
                    if (gui != null) {
                        gui.updateStaffAssignment(patient.getId(), member.getId(), member.getRole());
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isStaffQualifiedForCategory(Staff staff, int category) {
        switch (staff.getRole()) {
            case "Consultant":
            case "Registrar":
                return category <= 2; // Can only treat Category 1-2
                
            case "Senior Resident":
            case "Junior Resident":
                return true; // Can treat all categories
                
            case "Intern":
                return category >= 3; // Can only treat Category 3-5
                
            default:
                return false;
        }
    }
    
    private void releaseAllAssignedStaff(Patient patient) {
        for (Staff member : staff) {
            if (member.getAssignedPatients().contains(patient)) {
                member.releasePatient(patient, currentTime);
            }
        }
    }
    
    private void updateTreatments() {
        for (Bed bed : beds) {
            if (bed.isOccupied()) {
                Patient patient = bed.getCurrentPatient();
                
                // Check if treatment is complete including post-treatment time
                double totalTime = currentTime - patient.getArrivalTime() - patient.getWaitTime();
                if (totalTime >= patient.getTotalTreatmentTime()) {
                    // Process any remaining diagnostic studies
                    boolean studiesComplete = true;
                    for (DiagnosticStudy study : patient.getStudies()) {
                        if (!study.isComplete(currentTime)) {
                            studiesComplete = false;
                            break;
                        }
                    }
                    
                    // Only release if treatment and all studies are complete
                    if (studiesComplete) {
                        releasePatient(bed);
                    }
                }
            }
        }
    }
    
    private void releasePatient(Bed bed) {
        Patient patient = bed.getCurrentPatient();
        patient.setDischargeTime(currentTime);
        
        for (Staff member : staff) {
            if (member.getAssignedPatients().contains(patient)) {
                member.releasePatient(patient, currentTime);
            }
        }
        
        bed.releasePatient(currentTime);
    }
    
    public String generateReport() {
        metrics.setCurrentTime(currentTime);  // Ensure current time is up to date
        return metrics.generateReport();
    }
    
    public void threadStep() {
        currentTime++;

        // Check for new patient arrival
        if (Math.random() < 0.1) {
            int category = (int) (Math.random() * 5) + 1;
            Patient newPatient = new Patient((int) currentTime, category, currentTime);
            queues.get(category - 1).enqueue(newPatient);
            
            // Update GUI for new patient arrival
            if (gui != null) {
                gui.updatePatientArrival((int)currentTime);
                gui.updateCategoryAssignment((int)currentTime, category);
            }
        }

        // Process queues
        for (PatientQueue queue : queues) {
            if (!queue.isEmpty()) {
                Patient patient = queue.peek();
                
                // Try to find an empty bed first
                Bed availableBed = findEmptyBed(patient.getCategory());
                if (availableBed != null) {
                    // Found a bed, now assign staff
                    Staff availableStaff = findAvailableStaff(patient.getCategory());
                    if (availableStaff != null) {
                        // We have both bed and staff, proceed with assignment
                        patient.setWaitTime(currentTime - patient.getArrivalTime());  // Record wait time
                        availableBed.assignPatient(patient, currentTime);
                        availableStaff.assignPatient(patient, currentTime);
                        queue.dequeue();
                        
                        // Record metrics for this patient
                        metrics.recordPatient(patient);
                        
                        // Update GUI with assignments
                        if (gui != null) {
                            gui.updateBedAssignment(patient.getId(), availableBed.getId());
                            gui.updateStaffAssignment(patient.getId(), 
                                                    availableStaff.getId(), 
                                                    availableStaff.getRole());
                        }
                    }
                }
            }
        }

        // Process releases
        processReleases();
        updateTreatments();
        updateGUIStatus();
    }
    
    private Staff findAvailableStaff(int patientCategory) {
        String requiredRole;
        if (patientCategory <= 2)           requiredRole = "Consultant";
        else if (patientCategory == 3)      requiredRole = "Senior Resident";
        else                              requiredRole = "Junior Resident";
        
        for (Staff staff : this.staff)
            if (staff.getRole().equals(requiredRole) && staff.canTakePatient())
                return staff;
        return null;
    }
    
    private void startDiagnosticStudies(Patient patient) {
        double currentStudyTime = currentTime;
        for (DiagnosticStudy study : patient.getStudies()) {
            study.start(currentStudyTime);
            currentStudyTime += study.getDuration(); //we add all the studies' durations since they were performed sequentially
        }
    }
    
    private void processReleases() {
        for (Bed bed : beds)
            if (bed.isOccupied()) {
                Patient patient = bed.getCurrentPatient();
                if (currentTime >= patient.getArrivalTime() + patient.getTotalTreatmentTime()) {
                    patient.setDischargeTime(currentTime);  //we set this patient's discharge time before releasing him
                    releasePatient(bed);
                    metrics.updatePatientCompletion(patient); //updating final metrics of this patient
                }
            }
    }
    
    public double getCurrentTime() {
    	return currentTime;
    }
    
    public void updateGUIStatus() {
        if (gui != null) {
            // Update patient queues
            int[] waitingPatients = new int[MAX_QUEUES];
            for (int i = 0; i < queues.size(); i++)
                waitingPatients[i] = queues.get(i).getSize();
            
            //updating all the statuses in GUI
            gui.updatePatientStatus(waitingPatients);
            gui.updateBedStatus(beds);
            gui.updateStaffStatus(staff);
        }
    }
}
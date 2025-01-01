package trialCLASSES;

import java.util.ArrayList;

public class Hospital {
	private final int MAX_QUEUES = 5;  //max number of priority queues (one for each triage category)
    private final double INTERARRIVAL_SCALER = 10.0;  //scaling factor for time between patient arrivals
    
    //Variables for simulation
    private double currentTime;  // current simulation time in minutes
    private double simulationDuration;  // total duration to run the simulation
    private int nextPatientId;
    private double nextArrivalTime;
    
    //Important Variables for the class
    private ArrayList<PatientQueue> queues;  //priority queues for each triage category
    private ArrayList<Bed> beds;  			 //available hospital beds
    private ArrayList<Staff> staff;          //available medical staff
    private ResourceManager resourceManager; //used for beds and staff allocation management
    private SimulationMetrics metrics;       //tracks and also calculates simulation metrics
    private HospitalSimulationGUI gui;       //holds the reference of GUI
    private DiseaseManager diseaseManager;   //manages the disease given to patients
    
    public Hospital(double simulationDuration, int bedCount, int staffCount, HospitalSimulationGUI gui) {
        this.simulationDuration = simulationDuration;
        currentTime = 0;
        nextPatientId = 1;
        this.gui = gui;
        
        queues = new ArrayList<>();
        for (int i = 0; i < MAX_QUEUES; i++)
            queues.add(new PatientQueue(i + 1));
        
        resourceManager = new ResourceManager(beds, staff);
        beds = resourceManager.initializeBeds(bedCount);
        staff = resourceManager.initializeStaff(staffCount);
        metrics = new SimulationMetrics(beds, staff);
        diseaseManager = new DiseaseManager();
    }
    
    public void runSimulation() {
        while (currentTime < simulationDuration) {
            if (currentTime >= nextArrivalTime) {
                generatePatient();
                
                // schedule next arrival using Weibull distribution
                double interarrivalTime = Probability.generateInterarrivalTime(currentTime) * INTERARRIVAL_SCALER;
                nextArrivalTime = currentTime + Math.max(1.0, interarrivalTime); //ensuring minimum 1 second gap
            }
            
            processQueues();  //process the waiting queues
            updateTreatments(); //updating ongoing treatments
            currentTime++;
        }
        
        System.out.println(generateReport());
    }
    

    //Handle initial patient creation, diagnostic study assignments, and queue placement.
    private void generatePatient() {
    	Disease disease = diseaseManager.selectRandomDisease();
        int category = diseaseManager.selectCategoryForDisease(disease);
        Patient patient = new Patient(nextPatientId, category, currentTime, disease.getName());
        
        if (gui != null) {
            gui.updatePatientArrival(nextPatientId);
            gui.updateCategoryAssignment(nextPatientId, category);
        }
        
        //adding diagnostic studies based on severity
        if (category <= 3) {
            double bloodworkTime = Probability.generatePostTreatmentTime(currentTime) * 0.2;
            patient.addDiagnosticStudy("Blood Work", bloodworkTime);
            
            if (category <= 2) {
                double imagingTime = Probability.generatePostTreatmentTime(currentTime) * 0.3;
                patient.addDiagnosticStudy("Imaging", imagingTime);
            }
        }
        
       //add patient to appropriate queue based on category   
        queues.get(category - 1).enqueue(patient);
        nextPatientId++;
    }
    
    //Processes all patient queues in priority order. Also assign waiting patients to beds and staff
    private void processQueues() {
        for (PatientQueue queue : queues) {
            while (!queue.isEmpty()) {
                Patient patient = queue.viewNextPatient();
                
                //try to assign the patient to bed and a staff
                if (assignPatientToBed(patient)) {
                    queue.dequeue();  // remove patient from queue after assignment
                    patient.setWaitTime(currentTime - patient.getArrivalTime());
                    metrics.recordPatient(patient);
                }
                else {
                    break; // If we can't assign this patient, we can't assign others in this queue
                }
            }
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
    
     // Attempts to assign a patient to an appropriate bed and handles preemption for high-priority patients if necessary.
    private boolean assignPatientToBed(Patient patient) {
        //checking if we have available staff first, only then can we assign the patient to bed
        int requiredStaffCount = patient.getCategory() <= 2 ? 2 : 1; //we need 2 staffs for cat1 and cat2,while 1 for others
        int availableQualifiedStaff = resourceManager.countAvailableQualifiedStaff(patient.getCategory());
        
        if (availableQualifiedStaff < requiredStaffCount)
            return false;
        
        Bed assignedBed = resourceManager.findEmptyBed(patient.getCategory()); //using resource manager to assign the patient an empty bed
        
        // For Category 1 - 2, try preemption if no empty bed is available
        if (assignedBed == null && patient.getCategory() <= 2) {
            assignedBed = resourceManager.findPreemptableBed(patient.getCategory());
            if (assignedBed != null) {
                Patient preemptedPatient = assignedBed.getCurrentPatient();
                preemptedPatient.setPreempted(true);
                queues.get(preemptedPatient.getCategory() - 1).enqueue(preemptedPatient);
                resourceManager.releasePatient(preemptedPatient, assignedBed);
                metrics.recordPreemption();
            }
        }
        
        // If we found a bed (either empty or through preemption)
        if (assignedBed != null) {      //if assignedBed is null, that means there wasnt an empty or preempted bed
            assignedBed.assignPatient(patient, currentTime);
            if (gui != null) {
                gui.updateBedAssignment(patient.getId(), assignedBed.getId());  //updating gui textboxes
            }
            if (resourceManager.assignStaffToPatient(patient, gui)) { // Try to assign staff
                startDiagnosticStudies(patient);
                return true;
            } else {
                assignedBed.clearPatientFromBed(currentTime);  //releasing the bed if staff assignment fails
                return false;
            }
        }
        
        return false;
    }
    
    
     /* Update ongoing treatments and checks for completed patients.
        Process patient releases when treatment and diagnostic studies are complete. */
    private void updateTreatments() {
        for (Bed bed : beds) {
            if (bed.isOccupied()) {
                Patient patient = bed.getCurrentPatient();
                
                //checking if treatment is complete including post-treatment time
                double totalTime = currentTime - patient.getArrivalTime() - patient.getWaitTime();
                if (totalTime >= patient.getTotalTreatmentTime()) {
                    boolean studiesComplete = true;  //if the time has passed more than the treatment time, studies are complete
                    for (DiagnosticStudy study : patient.getStudies()) //processing all the remaining studies
                        if (!study.isComplete(currentTime)) {
                            studiesComplete = false;
                            break;
                        }
                    if (studiesComplete)
                        releasePatient(bed);  //releasing the patient if studies are complete
                }
            }
        }
    }
    
     // Release a patient from the hospital and update all resources and metrics
    private void releasePatient(Bed bed) {
        Patient patient = bed.getCurrentPatient();
        patient.setDischargeTime(currentTime);
        
        for (Staff member : staff)
            if (member.getAssignedPatients().contains(patient))
                member.removePatientFromStaff(patient, currentTime);
        
        bed.clearPatientFromBed(currentTime);
    }
    
    public String generateReport() {
        metrics.setCurrentTime(currentTime);  // Ensure current time is up to date
        return metrics.generateReport();
    }
    
    public void threadStep() {
        currentTime++;
        

        //checking for patient arrivals
        if (Math.random() < 0.1) {
        	Disease disease = diseaseManager.selectRandomDisease();
            int category = diseaseManager.selectCategoryForDisease(disease);

            Patient newPatient = new Patient((int)currentTime, category, currentTime, disease.getName());
            queues.get(category - 1).enqueue(newPatient);
            
            // Update GUI for new patient arrival
            if (gui != null) {
                gui.updatePatientArrival((int)currentTime);
                gui.updateCategoryAssignment((int)currentTime, category);
                gui.updateDiseaseAssignment(newPatient.getId(), disease.getName());
            }
        }
        
        
        // Process queues
        for (PatientQueue queue : queues) {
            if (!queue.isEmpty()) {
                Patient patient = queue.viewNextPatient();
                
                // Try to find an empty bed first
                Bed availableBed = resourceManager.findEmptyBed(patient.getCategory());
                if (availableBed != null) {
                    // Found a bed, now assign staff
                    Staff availableStaff = resourceManager.findAvailableStaff(patient.getCategory());
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
                            gui.updateStaffAssignment(patient.getId(),availableStaff.getId(),availableStaff.getRole());
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
    
    // Start all diagnostic studies for a patient in sequential order.
    private void startDiagnosticStudies(Patient patient) {
        double currentStudyTime = currentTime;
        for (DiagnosticStudy study : patient.getStudies()) {
            study.start(currentStudyTime);
            currentStudyTime += study.getDuration(); //we add all the studies' durations since they were performed sequentially
        }
    }
    
    // we use this to update the patients,bed and staff status textboxes in GUI
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
    
    public double getCurrentTime() {
    	return currentTime;
    }
}
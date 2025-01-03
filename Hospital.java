package COPY;

import java.util.ArrayList;

public class Hospital {
	private final int MAX_QUEUES = 5;  //max number of priority queues (one for each triage category)
    
    //Variables for simulation
    private double currentTime;  // current simulation time in minutes
    
    //Important Variables for the class
    private ArrayList<PatientQueue> queues;  //priority queues for each triage category
    private ArrayList<Bed> beds;  			 //available hospital beds
    private ArrayList<Staff> staff;          //available medical staff
    private ResourceManager resourceManager; //used for beds and staff allocation management
    private SimulationMetrics metrics;       //tracks and also calculates simulation metrics
    private HospitalSimulationGUI gui;       //holds the reference of GUI
    private DiseaseManager diseaseManager;   //manages the disease given to patients
    private CSVWriter csvWriter;
    
    public Hospital(int bedCount, int staffCount, HospitalSimulationGUI gui) {
        currentTime = 0;
        this.gui = gui;
        
        queues = new ArrayList<>();
        for (int i = 0; i < MAX_QUEUES; i++)
            queues.add(new PatientQueue(i + 1));
        
        resourceManager = new ResourceManager(beds, staff);
        beds = resourceManager.initializeBeds(bedCount);
        staff = resourceManager.initializeStaff(staffCount);
        metrics = new SimulationMetrics(beds, staff);
        diseaseManager = new DiseaseManager();
        this.csvWriter = new CSVWriter("CopyOfProcessed.csv");
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
        
        // Get all staff that handled this patient
        ArrayList<Staff> assignedStaff = new ArrayList<>();
        for (Staff member : staff) {
            if (member.getAssignedPatients().contains(patient)) {
                assignedStaff.add(member);
                member.removePatientFromStaff(patient, currentTime);
            }
        }
        
        // writing to CSV before clearing the bed
        csvWriter.writePatientData(patient, assignedStaff);
        
        bed.clearPatientFromBed(currentTime);
    }
    
    public String generateReport() {
        metrics.setCurrentTime(currentTime);  // Ensure current time is up to date
        return metrics.generateReport();
    }
    
    // we use this to update the patients,bed and staff status textboxes in GUI
    public void updateGUIStatus() {
        if (gui != null) {
            // update patient queues
            int[] waitingPatients = new int[MAX_QUEUES];
            for (int i = 0; i < queues.size(); i++)
                waitingPatients[i] = queues.get(i).getSize();
            
            //updating all the statuses in GUI
            gui.updatePatientStatus(waitingPatients);
            gui.updateBedStatus(beds);
            gui.updateStaffStatus(staff);
        }
    }
    
    
    //the main function to use the thread, count time, and simulate the entire program
    public void threadStep() {
        currentTime++;
      
        //checking for patient arrivals
        if (Math.random() < 0.1) {
        	Disease disease = diseaseManager.selectRandomDisease();
            int category = diseaseManager.selectCategoryForDisease(disease);

            Patient newPatient = new Patient(category, currentTime, disease.getName());
            queues.get(category - 1).enqueue(newPatient);
            
            // Update GUI for new patient arrival
            if (gui != null) {
                gui.updatePatientArrival(newPatient.getId());
                gui.updateCategoryAssignment(newPatient.getId(), category);
                gui.updateDiseaseAssignment(newPatient.getId(), disease.getName());
            }
        }
        
        
        // process queues
        for (PatientQueue queue : queues) {
            if (!queue.isEmpty()) {
                Patient patient = queue.viewNextPatient();
                
                //try to find an empty bed first
                Bed availableBed = resourceManager.findEmptyBed(patient.getCategory());
                if (availableBed != null) {
                    //found a bed, now assign staff
                    Staff availableStaff = resourceManager.findAvailableStaff(patient.getCategory());
                    if (availableStaff != null) {
                        // We have both bed and staff, proceed with assignment
                        patient.setWaitTime(currentTime - patient.getArrivalTime());  // Record wait time
                        availableBed.assignPatient(patient, currentTime);
                        availableStaff.assignPatient(patient, currentTime);
                        queue.dequeue();
                        
                        // Recording metrics for this patient
                        metrics.recordPatient(patient);
                        
                        // updating GUI with assignments
                        if (gui != null) {
                            gui.updateBedAssignment(patient.getId(), availableBed.getId());
                            gui.updateStaffAssignment(patient.getId(),availableStaff.getId(),availableStaff.getRole());
                        }
                    }
                }
            }
        }

        //process releases and update all required gui statuses
        processReleases();
        updateTreatments();
        updateGUIStatus();
    }
    
    public double getCurrentTime() {
    	return currentTime;
    }
}
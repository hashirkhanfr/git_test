package trial1;

public class Hospital {
    private Bed[] resuscitationBeds;
    private Bed[] acuteBeds;
    private Bed[] subacuteBeds;
    private Bed[] minorOpBeds;
    private PatientQueue[] queues;
    private HospitalStaff consultants;
    private HospitalStaff registrars;
    private HospitalStaff seniorResidents;
    private HospitalStaff juniorResidents;
    private HospitalStaff interns;
    private double currentTime;
    private double simulationDuration;
    private int patientID;

    public Hospital(double simulationDuration, int resuscitationBedCount, int acuteBedCount, int subacuteBedCount, int minorOpBedCount,
                    int consultantCount, int registrarCount, int seniorResidentCount, int juniorResidentCount, int internCount) {
        this.simulationDuration = simulationDuration;
        this.currentTime = 0;
        this.patientID = 1;

        // Initialize beds
        resuscitationBeds = initializeBeds(resuscitationBedCount, "Resuscitation Bed");
        acuteBeds = initializeBeds(acuteBedCount, "Acute Bed");
        subacuteBeds = initializeBeds(subacuteBedCount, "Subacute Bed");
        minorOpBeds = initializeBeds(minorOpBedCount, "Minor Operation Bed");

        // Initialize queues
        queues = new PatientQueue[5];
        for (int i = 0; i < 5; i++) {
            queues[i] = new PatientQueue(i + 1, 100); // Arbitrary capacity for now
        }

        // Initialize staff
        consultants = new HospitalStaff("Consultant", consultantCount);
        registrars = new HospitalStaff("Registrar", registrarCount);
        seniorResidents = new HospitalStaff("Senior Resident", seniorResidentCount);
        juniorResidents = new HospitalStaff("Junior Resident", juniorResidentCount);
        interns = new HospitalStaff("Intern", internCount);
    }

    private Bed[] initializeBeds(int count, String type) {
        Bed[] beds = new Bed[count];
        for (int i = 0; i < count; i++) {
            beds[i] = new Bed(type);
        }
        return beds;
    }
    
    public void addPatientToQueue(Patient patient) {
        int category = patient.getCategory();
        if (category >= 1 && category <= 5) {
            queues[category - 1].addPatient(patient); // Add patient to the corresponding queue
            System.out.println("Patient added to queue: " + patient);
        } else {
            System.out.println("Invalid category for patient: " + patient);
        }
    }

 // Inside the Hospital class
    public void runSimulation() {
        while (currentTime < simulationDuration) {
            // 1. Generate a new patient
            if (Math.random() < 0.1) { // Example probability of patient arrival
                int category = (int) (Math.random() * 5) + 1; // Random category between 1 and 5
                double arrivalTime = currentTime;
                double treatmentTime = 15 + Math.random() * 45; // Random treatment time between 15 and 60 minutes
                Patient patient = new Patient(patientID++, arrivalTime, 0, treatmentTime, 0, category);
                queues[category - 1].addPatient(patient);
            }

            // 2. Assign patients to beds
            assignPatientsToBeds();

            // 3. Treat patients
            treatPatients();

            // 4. Update time
            currentTime += 1; // Increment time in seconds
        }

        // Simulation finished, print results or handle data
        System.out.println("Simulation finished.");
    }

    void assignPatientsToBeds() {
        for (PatientQueue queue : queues) {
            while (!queue.isEmpty()) {
                Patient patient = queue.removePatient();
                Bed[] availableBeds = getBedsForCategory(patient.getCategory());
                if (assignToAvailableBed(patient, availableBeds)) {
                    break; // Stop looking for beds if one is assigned
                }
            }
        }
    }

    private Bed[] getBedsForCategory(int category) {
        switch (category) {
            case 1:
                return resuscitationBeds;
            case 2:
                return acuteBeds;
            case 3:
                return subacuteBeds;
            case 4:
            case 5:
                return minorOpBeds;
            default:
                return new Bed[0];
        }
    }

    private boolean assignToAvailableBed(Patient patient, Bed[] beds) {
        for (Bed bed : beds) {
            if (!bed.isOccupied()) {
                bed.assignPatient(patient);
                patient.setBedAssigned(bed.getType());
                return true;
            }
        }
        return false;
    }

    void treatPatients() {
        treatBeds(resuscitationBeds);
        treatBeds(acuteBeds);
        treatBeds(subacuteBeds);
        treatBeds(minorOpBeds);
    }

    private void treatBeds(Bed[] beds) {
        for (Bed bed : beds) {
            if (bed.isOccupied()) {
                Patient patient = bed.getPatient();
                double treatmentTimeLeft = patient.getTreatmentTime() - (currentTime - patient.getArrivalTime());
                if (treatmentTimeLeft <= 0) {
                    bed.releasePatient(); // Patient is discharged
                }
            }
        }
    }

	public double getSimulationDuration() {
		return simulationDuration;
	}


}

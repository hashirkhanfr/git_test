package COPY;
import java.util.ArrayList;

public class SimulationMetrics {
	private double totalWaitTime;
    private int totalPatients;
    private int patientsSeenOnTime;
    private int totalPreemptions;
    private ArrayList<Double> categoryWaitTimes;
    private ArrayList<Integer> categoryPatientCounts;
    private double totalThroughputTime;
    private ArrayList<Bed> beds;          // Added to track bed statistics
    private ArrayList<Staff> staff;       // Added to track staff statistics
    private double currentTime;           // Added to calculate utilization

    public SimulationMetrics(ArrayList<Bed> beds, ArrayList<Staff> staff) {
        this.totalWaitTime = 0;
        this.totalPatients = 0;
        this.patientsSeenOnTime = 0;
        this.totalPreemptions = 0;
        this.totalThroughputTime = 0;
        this.beds = beds;
        this.staff = staff;
        this.categoryWaitTimes = new ArrayList<>();
        this.categoryPatientCounts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            categoryWaitTimes.add(0.0);
            categoryPatientCounts.add(0);
        }
    }
    
    public void recordPatient(Patient patient) {
        int category = patient.getCategory() - 1;
        categoryWaitTimes.set(category, categoryWaitTimes.get(category) + patient.getWaitTime());
        categoryPatientCounts.set(category, categoryPatientCounts.get(category) + 1);
        totalPatients++;
        totalWaitTime += patient.getWaitTime();
        
        double recommTime = getRecommendedWaitTime(patient.getCategory());
        if (patient.getWaitTime() <= recommTime * 60)  //if the wait time was equal or less than recommended time, patient was seen on time
            patientsSeenOnTime++;
    }
    
    public void updatePatientCompletion(Patient patient) {
        double throughputTime = patient.getDischargeTime() - patient.getArrivalTime();
        totalThroughputTime += throughputTime;
    }
    
    public void recordPreemption() {
        totalPreemptions++;
    }
    
    private double getRecommendedWaitTime(int category) {
        switch (category) {
            case 1: return 0;    // Immediate
            case 2: return 10;   // 10 minutes
            case 3: return 30;   // 30 minutes
            case 4: return 60;   // 1 hour
            case 5: return 120;  // 2 hours
            default: return 0;
        }
    }

    public String generateReport() {
    	String report = "===================================> Simulation Results <===================================\n";
    	report += "Simulation Duration: " + currentTime / 60.0 + " minutes\n\n";
    	report += "Total Patients Processed: " + totalPatients + "\n";

    	if (totalPatients > 0) {
    	    report += String.format("Average Wait Time: %.1f minutes\n",(totalWaitTime / 60.0) / totalPatients);
    	    report += String.format("Average Total Time in System: %.1f minutes\n",(totalThroughputTime / 60.0) / totalPatients);
    	    report += String.format("Patients Seen Within Target Time: %.1f%%\n",(patientsSeenOnTime * 100.0) / totalPatients);
    	}

    	report += String.format("Total Preemptions: %d\n", totalPreemptions);

    	// Category statistics
    	report += "\n==================================> By Category Statistics <==================================\n";
    	for (int i = 0; i < 5; i++) {
    	    int count = categoryPatientCounts.get(i);
    	    if (count > 0) {
    	        double avgWait = (categoryWaitTimes.get(i) / 60.0) / count;
    	        report += String.format(" ---------------> Category %d <---------------\n", i + 1);
    	        report += String.format(" -- Patients: %d\n", count);
    	        report += String.format(" -- Average Wait: %.1f minutes\n", avgWait);
    	    }
    	}

    	double totalBedUtilization = 0;
    	for (Bed bed : beds)
    	    totalBedUtilization += bed.getUtilization(currentTime);
    	double avgBedUtilization = totalBedUtilization / beds.size();
    	report += String.format("\nAverage Bed Utilization: %.1f%%\n", avgBedUtilization * 100);

    	double totalStaffUtilization = 0;
    	for (Staff member : staff)
    	    totalStaffUtilization += member.getUtilization(currentTime);
    	double avgStaffUtilization = totalStaffUtilization / staff.size();
    	report += String.format("Average Staff Utilization: %.1f%%\n", avgStaffUtilization * 100);

    	return report;
    }
    
    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }
}
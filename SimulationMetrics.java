package trial2claude;

public class SimulationMetrics {
    private double totalWaitTime;
    private int totalPatients;
    private int patientsSeenOnTime;
    private int totalPreemptions;
    private double[] categoryWaitTimes;
    private int[] categoryPatientCounts;
    
    public SimulationMetrics() {
        this.categoryWaitTimes = new double[5];
        this.categoryPatientCounts = new int[5];
    }
    
    public void recordPatient(Patient patient) {
        int category = patient.getCategory() - 1;
        categoryWaitTimes[category] += patient.getWaitTime();
        categoryPatientCounts[category]++;
        totalPatients++;
        totalWaitTime += patient.getWaitTime();
        
        // Check if patient was seen within recommended time for their category
        double recommendedTime = getRecommendedWaitTime(patient.getCategory());
        if (patient.getWaitTime() <= recommendedTime) {
            patientsSeenOnTime++;
        }
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
            default: return Double.MAX_VALUE;
        }
    }
    
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("======== Simulation Results =======\n");
        report.append(String.format("Total Patients = %d\n", totalPatients));
        report.append(String.format("Average Wait Time = %.2f minutes\n", 
            totalPatients > 0 ? totalWaitTime / totalPatients : 0));
        report.append(String.format("Patients Seen On Time = %.1f%%\n",
            totalPatients > 0 ? (patientsSeenOnTime * 100.0 / totalPatients) : 0));
        report.append(String.format("Total Preemptions = %d\n", totalPreemptions));
        
        for (int i = 0; i < 5; i++) {
            if (categoryPatientCounts[i] > 0) {
                report.append(String.format("Category %d Average Wait: %.2f minutes\n",
                    i + 1, categoryWaitTimes[i] / categoryPatientCounts[i]));
            }
        }
        
        return report.toString();
    }
}

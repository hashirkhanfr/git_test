package trial1;

public class Patient {
    private int id;
    private double arrivalTime;
    private double waitTime;
    private double treatmentTime;
    private double dischargeTime;
    private int category;
    private String bedAssigned;

    public Patient(int id, double arrivalTime, double waitTime, double treatmentTime, double dischargeTime, int category) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.waitTime = waitTime;
        this.treatmentTime = treatmentTime;
        this.dischargeTime = dischargeTime;
        this.category = category;
        this.bedAssigned = "None";
    }

    // Getters and setters
    public int getId() { return id; }
    public double getArrivalTime() { return arrivalTime; }
    public double getWaitTime() { return waitTime; }
    public double getTreatmentTime() { return treatmentTime; }
    public double getDischargeTime() { return dischargeTime; }
    public int getCategory() { return category; }
    public String getBedAssigned() { return bedAssigned; }

    public void setBedAssigned(String bedAssigned) {
        this.bedAssigned = bedAssigned;
    }

    @Override
    public String toString() {
        return String.format("Patient ID: %d\nCategory: %d\nArrival Time: %.2f\nWait Time: %.2f\nTreatment Time: %.2f\nDischarge Time: %.2f\nBed Assigned: %s\n",
                id, category, arrivalTime, waitTime, treatmentTime, dischargeTime, bedAssigned);
    }
}

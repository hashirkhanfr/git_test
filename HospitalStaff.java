package trial1;

public class HospitalStaff {
    private String role;
    private int availableCount;

    public HospitalStaff(String role, int count) {
        this.role = role;
        this.availableCount = count;
    }

    public int getAvailableCount() { return availableCount; }
    public String getRole() { return role; }

    public void assignStaff(int count) {
        availableCount -= count;
    }

    public void releaseStaff(int count) {
        availableCount += count;
    }
}

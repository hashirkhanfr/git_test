package trial2claude;

public class DiagnosticStudy {
    private String type;
    private double duration;
    private boolean completed;
    private double startTime;
    
    public DiagnosticStudy(String type, double duration) {
        this.type = type;
        this.duration = duration;
        this.completed = false;
        this.startTime = -1;
    }
    
    public void start(double time) {
        this.startTime = time;
    }
    
    public boolean isComplete(double currentTime) {
        return startTime >= 0 && (currentTime - startTime) >= duration;
    }
    
    public String getType() { return type; }
    public double getDuration() { return duration; }
}

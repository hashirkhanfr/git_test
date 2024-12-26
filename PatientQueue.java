package trial2claude;

public class PatientQueue {
    private Patient[] patients;
    private int front;
    private int rear;
    private int size;
    private final int capacity;
    private final int priority;
    
    public PatientQueue(int priority, int capacity) {
        this.priority = priority;
        this.capacity = capacity;
        this.patients = new Patient[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }
    
    public void enqueue(Patient patient) {
        if (size == capacity) {
            throw new IllegalStateException("Queue is full");
        }
        rear = (rear + 1) % capacity;
        patients[rear] = patient;
        size++;
    }
    
    public Patient dequeue() {
        if (size == 0) {
            return null;
        }
        Patient patient = patients[front];
        front = (front + 1) % capacity;
        size--;
        return patient;
    }
    
    public Patient peek() {
        return size == 0 ? null : patients[front];
    }
    
    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == capacity; }
    public int getSize() { return size; }
    public int getPriority() { return priority; }
}

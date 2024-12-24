package trial1;

public class PatientQueue {
    private int priority;
    private Patient[] patients;
    private int front;
    private int rear;
    private int capacity;
    private int size;

    public PatientQueue(int priority, int capacity) {
        this.priority = priority;
        this.capacity = capacity;
        this.patients = new Patient[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    public void addPatient(Patient patient) {
        if (size == capacity) {
            throw new IllegalStateException("Queue is full");
        }
        rear = (rear + 1) % capacity;
        patients[rear] = patient;
        size++;
    }

    public Patient removePatient() {
        if (size == 0) {
            throw new IllegalStateException("Queue is empty");
        }
        Patient patient = patients[front];
        front = (front + 1) % capacity;
        size--;
        return patient;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int getPriority() { return priority; }
}

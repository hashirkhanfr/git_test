package COPY;

import java.util.ArrayList;

public class ResourceManager {
	private ArrayList<Bed> beds;
	private ArrayList<Staff> staff;
	
	 public ResourceManager(ArrayList<Bed> beds, ArrayList<Staff> staff) {
	     this.beds = beds;
	     this.staff = staff;
	 }
	 
	 public ArrayList<Bed> initializeBeds(int count) {
	     beds = new ArrayList<>();
	     for (int i = 0; i < count; i++) {
	         String type;
	         if (i < count/4)           type = "Resuscitation";
	         else if (i < count/2)      type = "Acute";
	         else if (i < 3*count/4)    type = "Subacute";
	         else                       type = "Minor";
	         beds.add(new Bed(i + 1, type));
	     }
	     return beds;
	 }
	 
	 public ArrayList<Staff> initializeStaff(int count) {
	     staff = new ArrayList<>();
	     for (int i = 0; i < count; i++) {
	         String role;
	         
	         if (i < count * 0.25)             role = "Consultant"; 
	         else if (i < count * 0.6)         role = "Registrar"; 
	         else if (i < count * 0.8)         role = "Senior Resident";
	         else if (i < count * 0.9)         role = "Junior Resident"; 
	         else                              role = "Intern";
	         
	         int maxPatients;
	         if (role.equals("Consultant"))             maxPatients = 1;
	         else if (role.equals("Registrar"))         maxPatients = 2;
	         else if (role.equals("Senior Resident"))   maxPatients = 2;
	         else if (role.equals("Junior Resident"))   maxPatients = 3;
	         else                                       maxPatients = 2;
	         
	         staff.add(new Staff((i+1), role, maxPatients));
	     }
	     return staff;
	 }
	
	 // finding empty bed from a bed arrayList
	 public Bed findEmptyBed(int category) {
	     String requiredType = getBedTypeForCategory(category);
	     for (Bed bed : beds) {
	         if (!bed.isOccupied() && bed.getType().equals(requiredType)) {
	             return bed;
	         }
	     }
	     return null;
	 }
	 
	 public Staff findAvailableStaff(int patientCategory) {
	        String[] rolePriority;
	        
	        // defining role priorities based on patient category
	        if (patientCategory <= 2) {
	            rolePriority = new String[] {"Consultant", "Senior Resident", "Registrar"};
	        } else if (patientCategory == 3) {
	            rolePriority = new String[] {"Senior Resident", "Registrar", "Junior Resident"};
	        } else {
	            rolePriority = new String[] {"Intern", "Junior Resident", "Registrar"};
	        }
	        
	        // Attempt to find an available staff member for each role in priority order
	        for (String role : rolePriority) {
	            for (Staff staffMember : staff) {
	                if (staffMember.getRole().equals(role) && staffMember.canTakePatient() && isStaffQualifiedForCategory(staffMember, patientCategory)) {
	                    return staffMember;
	                }
	            }
	        }
	        
	        return null; // No available staff found
	 }
 
	
	 private boolean isStaffQualifiedForCategory(Staff staff, int category) {
	     switch (staff.getRole()) {
	         case "Consultant":
	         case "Registrar":
	             return category <= 2;
	         case "Senior Resident":
	         case "Junior Resident":
	             return true;
	         case "Intern":
	             return category >= 3;
	         default:
	             return false;
	     }
	 }
	
	 private static String getBedTypeForCategory(int category) {
	     switch (category) {
	         case 1: return "Resuscitation";
	         case 2: return "Acute";
	         case 3: return "Subacute";
	         default: return "Minor";
	     }
	 }
	 
	 public ArrayList<Bed> getBeds() { return beds; }
	 public ArrayList<Staff> getStaff() { return staff; }
}
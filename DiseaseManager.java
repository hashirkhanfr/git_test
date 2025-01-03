package COPY;

import java.util.ArrayList;

public class DiseaseManager {
    private ArrayList<Disease> diseases;
    private double totalArrivalRate;
    
    public DiseaseManager() {
        diseases = new ArrayList<>();
        initializeDiseases();
    }
    
    private void initializeDiseases() {
        // adding all diseases with their category probabilities and arrival rates [cat1, cat2, cat3, cat4, cat5]      
        // critical conditions have higher probabilities for categories 1 and 2
        diseases.add(new Disease("Multitrauma",new double[]{0.30, 0.40, 0.20, 0.08, 0.02}, 0.05));
            
        diseases.add(new Disease("Blood/Immune",new double[]{0.15, 0.25, 0.35, 0.15, 0.10}, 0.04));
            
        diseases.add(new Disease("Cardiac/Vascular", new double[]{0.25, 0.35, 0.25, 0.10, 0.05}, 0.08));
            
        diseases.add(new Disease("Diabetes/Endocrine", 
            new double[]{0.10, 0.20, 0.40, 0.20, 0.10}, 0.05));
            
        diseases.add(new Disease("DNW Prior to Triage", 
            new double[]{0.05, 0.15, 0.30, 0.30, 0.20}, 0.02));
            
        diseases.add(new Disease("Drug/Alcohol/Poisoning", 
            new double[]{0.20, 0.30, 0.30, 0.15, 0.05}, 0.06));
            
        diseases.add(new Disease("ENT/Oral", 
            new double[]{0.05, 0.15, 0.35, 0.30, 0.15}, 0.04));
            
        diseases.add(new Disease("Environmental/Temperature/MISC", 
            new double[]{0.10, 0.20, 0.30, 0.25, 0.15}, 0.03));
            
        diseases.add(new Disease("Gastrointestinal", 
            new double[]{0.10, 0.25, 0.35, 0.20, 0.10}, 0.08));
            
        diseases.add(new Disease("GP referred/Hosp transfer", 
            new double[]{0.15, 0.25, 0.35, 0.15, 0.10}, 0.04));
            
        diseases.add(new Disease("Injury", 
            new double[]{0.15, 0.25, 0.35, 0.15, 0.10}, 0.10));
            
        diseases.add(new Disease("Neurological", 
            new double[]{0.20, 0.30, 0.30, 0.15, 0.05}, 0.07));
            
        diseases.add(new Disease("Eye", 
            new double[]{0.05, 0.15, 0.35, 0.30, 0.15}, 0.03));
            
        diseases.add(new Disease("Nonemergent Review", 
            new double[]{0.02, 0.08, 0.25, 0.35, 0.30}, 0.04));
            
        diseases.add(new Disease("Obstetrics/Gynaecology", 
            new double[]{0.10, 0.20, 0.40, 0.20, 0.10}, 0.05));
            
        diseases.add(new Disease("Paediatric", 
            new double[]{0.15, 0.25, 0.35, 0.15, 0.10}, 0.06));
            
        diseases.add(new Disease("Pain", 
            new double[]{0.05, 0.15, 0.35, 0.30, 0.15}, 0.07));
            
        diseases.add(new Disease("Psychiatric/Behavioural", 
            new double[]{0.10, 0.20, 0.35, 0.25, 0.10}, 0.05));
            
        diseases.add(new Disease("Regional Problems", 
            new double[]{0.05, 0.15, 0.35, 0.30, 0.15}, 0.03));
            
        diseases.add(new Disease("Renal", 
            new double[]{0.15, 0.25, 0.35, 0.15, 0.10}, 0.04));
            
        diseases.add(new Disease("Respiratory", 
            new double[]{0.20, 0.30, 0.30, 0.15, 0.05}, 0.08));
            
        diseases.add(new Disease("Urinary/Reproductive", 
            new double[]{0.10, 0.20, 0.35, 0.25, 0.10}, 0.05));
            
        for (Disease disease : diseases) {
            totalArrivalRate += disease.getArrivalRate();
        }
    }
    
    public Disease selectRandomDisease() {
        double random = Math.random() * totalArrivalRate;
        double cumulativeRate = 0;
        
        for (Disease disease : diseases) {
            cumulativeRate += disease.getArrivalRate();
            if (random <= cumulativeRate) {
                return disease;
            }
        }
        
        return diseases.get(diseases.size() - 1); // Fallback to last disease
    }
    
    public int selectCategoryForDisease(Disease disease) {
        double random = Math.random();
        double[] probs = disease.getCategoryProbabilities();
        double cumulative = 0;
        
        for (int i = 0; i < probs.length; i++) {
            cumulative += probs[i];
            if (random <= cumulative) {
                return i + 1; // Categories start from 1, not 0
            }
        }
        
        return 5; //fallback to category 5
    }
}

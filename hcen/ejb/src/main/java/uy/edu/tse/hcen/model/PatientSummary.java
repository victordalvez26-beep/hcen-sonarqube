package uy.edu.tse.hcen.model;

import jakarta.persistence.*;

@Entity
@Table(name = "patient_summary")
public class PatientSummary {

    @Id
    @Column(name = "uid", length = 255, nullable = false)
    private String uid;
    
    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;
    
    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;
    
    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications;
    
    @Column(name = "immunizations", columnDefinition = "TEXT")
    private String immunizations;
    
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;
    
    @Column(name = "procedures", columnDefinition = "TEXT")
    private String procedures;
    
    public PatientSummary() {
    }
    
    public PatientSummary(String uid) {
        this.uid = uid;
    }
    
    // Getters y Setters
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getAllergies() {
        return allergies;
    }
    
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    
    public String getConditions() {
        return conditions;
    }
    
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
    
    public String getMedications() {
        return medications;
    }
    
    public void setMedications(String medications) {
        this.medications = medications;
    }
    
    public String getImmunizations() {
        return immunizations;
    }
    
    public void setImmunizations(String immunizations) {
        this.immunizations = immunizations;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    public String getProcedures() {
        return procedures;
    }
    
    public void setProcedures(String procedures) {
        this.procedures = procedures;
    }
}


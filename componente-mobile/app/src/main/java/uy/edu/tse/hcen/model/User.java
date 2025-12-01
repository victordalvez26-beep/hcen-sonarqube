package uy.edu.tse.hcen.model;

import java.util.Date;

public class User {
    private String email;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String documentType;
    private String documentCode;
    private String nationality;
    private Date birthdate;
    private Department department;
    private String location;
    private String address;

    public User(String email, String firstName, String secondName,
                String firstLastName, String secondLastName,
                String documentType, String documentCode, String nationality,
                Date birthdate, Department department,
                String location, String address) {
        this.email = email;
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.documentType = documentType;
        this.documentCode = documentCode;
        this.nationality = nationality;
        this.birthdate = birthdate;
        this.department = department;
        this.location = location;
        this.address = address;
    }

    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getSecondName() { return secondName; }
    public String getFirstLastName() { return firstLastName; }
    public String getSecondLastName() { return secondLastName; }
    public String getDocumentType() { return documentType; }
    public String getDocumentCode() { return documentCode; }
    public String getNationality() { return nationality; }
    public Date getBirthdate() { return birthdate; }
    public Department getDepartamento() { return department; }
    public String getLocation() { return location; }
    public String getAddress() { return address; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName).append(" ");
        if (secondName != null && !secondName.isEmpty()) sb.append(secondName).append(" ");
        if (firstLastName != null) sb.append(firstLastName).append(" ");
        if (secondLastName != null && !secondLastName.isEmpty()) sb.append(secondLastName);
        return sb.toString().trim();
    }
}
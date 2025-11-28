package com.example.programminggroupproject.model;

public class User {

    private String username;
    private String role;
    private String fullName;
    private String email;

    public User(String username, String role, String fullName, String email) {
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }

    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
}
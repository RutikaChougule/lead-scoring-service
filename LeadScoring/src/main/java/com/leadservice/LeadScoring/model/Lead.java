package com.leadservice.LeadScoring.model;

public class Lead {
    private String name;
    private String role;
    private String company;
    private String industry;
    private String location;
    private String linkedinBio;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLinkedinBio() {
        return linkedinBio;
    }

    public void setLinkedinBio(String linkedinBio) {
        this.linkedinBio = linkedinBio;
    }
}

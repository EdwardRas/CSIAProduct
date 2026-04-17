package com.example.application.pilots;

public class Pilot {
    private Long id;
    public String name;
    private String licenseNumber;
    public boolean isFlying;

    public Pilot() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}

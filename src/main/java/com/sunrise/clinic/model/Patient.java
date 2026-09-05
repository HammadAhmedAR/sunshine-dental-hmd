package com.sunrise.clinic.model;

public record Patient(long id, String fullName, String address, String phone) {
    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
}

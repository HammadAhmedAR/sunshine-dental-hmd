package com.sunrise.clinic.model;

public record Dentist(long id, String fullName) {
    public long getId() { return id; }
    public String getIdValue() { return Long.toString(id); }
    public String getFullName() { return fullName; }
}

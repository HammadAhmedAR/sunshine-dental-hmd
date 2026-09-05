package com.sunrise.clinic.model;

public record BillPreview(AppointmentDetails appointment, BillAmounts amounts) {
    public AppointmentDetails getAppointment() { return appointment; }
    public BillAmounts getAmounts() { return amounts; }
}

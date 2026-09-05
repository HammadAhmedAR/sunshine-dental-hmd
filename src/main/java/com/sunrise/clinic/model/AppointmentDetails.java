package com.sunrise.clinic.model;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;

/** Joined appointment projection used by services and views; no credentials. */
public record AppointmentDetails(long id, String appointmentNumber, long patientId, String patientName,
        String address, String phone, long dentistId, String dentistName, long treatmentId, String treatmentName,
        BigDecimal treatmentFee, Instant startsAt, Instant endsAt, AppointmentStatus status, Instant createdAt,
        boolean billed) {
    private static final ZoneId ZONE = ZoneId.of("Asia/Colombo");
    public String getAppointmentNumber() { return appointmentNumber; }
    public String getPatientName() { return patientName; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getDentistName() { return dentistName; }
    public String getTreatmentName() { return treatmentName; }
    public BigDecimal getTreatmentFee() { return treatmentFee; }
    public String getDate() { return startsAt.atZone(ZONE).toLocalDate().toString(); }
    public String getTime() { return startsAt.atZone(ZONE).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")); }
    public String getCreatedAt() { return createdAt.atZone(ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); }
    public String getStatus() { return status.name(); }
    public boolean isBilled() { return billed; }
    public boolean isEditable() { return status == AppointmentStatus.BOOKED && !billed; }
    public String getDentistIdValue() { return Long.toString(dentistId); }
    public String getTreatmentIdValue() { return Long.toString(treatmentId); }
}

package com.sunrise.clinic.model;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;

public record Bill(long id, String billNumber, String appointmentNumber, String patientName, String dentistName,
        String treatmentName, BigDecimal treatmentCost, BigDecimal consultationFee, BigDecimal discount,
        BigDecimal total, Instant issuedAt) {
    public String getBillNumber() { return billNumber; }
    public String getAppointmentNumber() { return appointmentNumber; }
    public String getPatientName() { return patientName; }
    public String getDentistName() { return dentistName; }
    public String getTreatmentName() { return treatmentName; }
    public BigDecimal getTreatmentCost() { return treatmentCost; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotal() { return total; }
    public String getIssuedAt() { return issuedAt.atZone(ZoneId.of("Asia/Colombo")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); }
}

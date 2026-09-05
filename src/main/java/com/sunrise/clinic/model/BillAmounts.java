package com.sunrise.clinic.model;

import java.math.BigDecimal;

public record BillAmounts(BigDecimal treatmentCost, BigDecimal consultationFee, BigDecimal total) {
    public BigDecimal getTreatmentCost() { return treatmentCost; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public BigDecimal getTotal() { return total; }
}

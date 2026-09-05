package com.sunrise.clinic.model;

import java.math.BigDecimal;

public record RevenueSummary(long billCount, BigDecimal treatmentRevenue, BigDecimal consultationRevenue,
        BigDecimal discounts, BigDecimal totalRevenue) {
    public long getBillCount() { return billCount; }
    public BigDecimal getTreatmentRevenue() { return treatmentRevenue; }
    public BigDecimal getConsultationRevenue() { return consultationRevenue; }
    public BigDecimal getDiscounts() { return discounts; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}

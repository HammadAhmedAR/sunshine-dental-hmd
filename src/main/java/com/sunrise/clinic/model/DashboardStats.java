package com.sunrise.clinic.model;

public record DashboardStats(long today, long upcoming) {
    public long getToday() { return today; }
    public long getUpcoming() { return upcoming; }
}

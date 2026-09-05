package com.sunrise.clinic.model;

import java.math.BigDecimal;

public record Treatment(long id, String name, BigDecimal price, int durationMinutes) {
    public long getId() { return id; }
    public String getIdValue() { return Long.toString(id); }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getDurationMinutes() { return durationMinutes; }
}

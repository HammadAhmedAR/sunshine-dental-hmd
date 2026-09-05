package com.sunrise.clinic.service;

import com.sunrise.clinic.model.AppointmentStatus;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class QueryValidation {
    private QueryValidation() { }
    public static String reference(String text) {
        String value = text == null ? "" : text.trim();
        if (value.length() > 32 || !value.matches("(?:SDC-[0-9]+|APT-[0-9]{4}-[0-9]{5,})")) {
            throw new ValidationException("Enter an appointment number such as APT-2026-00001 or SDC-1.");
        }
        return value;
    }
    public static LocalDate date(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) throw new ValidationException("Select a date.");
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value);
            if (date.getYear() < 2000 || date.getYear() > 9999) throw new DateTimeParseException("year", value, 0);
            return date;
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Enter a valid date in YYYY-MM-DD format.");
        }
    }
    public static AppointmentStatus status(String value) {
        if (value == null || value.isBlank()) return null;
        try { return AppointmentStatus.valueOf(value); }
        catch (IllegalArgumentException exception) { throw new ValidationException("Select a valid appointment status."); }
    }
    public static int page(String value) {
        if (value == null || value.isBlank()) return 1;
        try {
            int page = Integer.parseInt(value);
            if (page >= 1 && page <= 100000) return page;
        } catch (NumberFormatException ignored) { }
        throw new ValidationException("Select a valid page number.");
    }
}

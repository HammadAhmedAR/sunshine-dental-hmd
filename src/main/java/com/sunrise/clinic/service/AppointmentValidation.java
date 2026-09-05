package com.sunrise.clinic.service;

import java.time.*;
import java.time.format.DateTimeParseException;

/** Shared registration/rescheduling rules; HTTP input parsing remains in the service layer. */
final class AppointmentValidation {
    private AppointmentValidation() { }
    static long requiredId(String text, String message) {
        try {
            long id = Long.parseLong(text == null ? "" : text);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) { }
        throw new ValidationException(message);
    }
    static Instant start(String date, String time) {
        if (date == null || date.isBlank()) throw new ValidationException("Appointment date is required.");
        if (time == null || time.isBlank()) throw new ValidationException("Appointment time is required.");
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            LocalTime parsedTime = LocalTime.parse(time);
            if (parsedTime.getSecond() != 0 || parsedTime.getNano() != 0
                    || parsedDate.getYear() < 2000 || parsedDate.getYear() > 9999) {
                throw new ValidationException("Enter a valid date and a time in whole minutes.");
            }
            return LocalDateTime.of(parsedDate, parsedTime).atZone(DashboardService.CLINIC_ZONE).toInstant();
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Enter a valid appointment date and time.");
        }
    }
    static void notPast(Instant start, Clock clock) {
        if (start.isBefore(clock.instant())) throw new ValidationException("Appointments cannot be booked in the past.");
    }
}

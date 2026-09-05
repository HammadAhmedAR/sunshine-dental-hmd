package com.sunrise.clinic.util;

import java.time.LocalDate;
import java.util.Locale;

public final class AppointmentNumberGenerator {
    private AppointmentNumberGenerator() { }

    // The DAO obtains a global PostgreSQL sequence value in the registration transaction.
    // Use the visit's Sri Lankan calendar year; never reset the sequence or truncate it.
    // Existing SDC references are retained. PostgreSQL still enforces uniqueness.
    public static String generate(LocalDate appointmentDate, long sequence) {
        if (appointmentDate == null || appointmentDate.getYear() < 2000
                || appointmentDate.getYear() > 9999 || sequence <= 0) {
            throw new IllegalArgumentException("A supported appointment date and positive sequence are required.");
        }
        return String.format(Locale.ROOT, "APT-%04d-%05d", appointmentDate.getYear(), sequence);
    }
}

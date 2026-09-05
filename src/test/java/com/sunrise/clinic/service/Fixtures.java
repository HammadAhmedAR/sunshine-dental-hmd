package com.sunrise.clinic.service;

import com.sunrise.clinic.model.*;
import java.math.BigDecimal;
import java.time.Instant;

final class Fixtures {
    static final Instant NOW = Instant.parse("2026-09-05T06:00:00Z");
    static AppointmentDetails appointment(AppointmentStatus status, boolean billed, Instant start) {
        return new AppointmentDetails(1, "APT-2026-00001", 2, "Nimal Perera", "12 Lake Road", "+94771234567",
                3, "Dr. Perera", 4, "Filling", new BigDecimal("8000.00"), start, start.plusSeconds(1200), status, NOW, billed);
    }
}

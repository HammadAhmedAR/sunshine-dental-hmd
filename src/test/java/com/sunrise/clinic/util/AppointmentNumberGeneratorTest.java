package com.sunrise.clinic.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentNumberGeneratorTest {
    @Test void producesExpectedNonblankFormat() {
        String number = AppointmentNumberGenerator.generate(LocalDate.of(2026, 9, 6), 1);
        assertFalse(number.isBlank());
        assertEquals("APT-2026-00001", number);
    }
    @Test void usesAppointmentYearAtYearBoundary() {
        assertEquals("APT-2027-00042", AppointmentNumberGenerator.generate(LocalDate.of(2027, 1, 1), 42));
    }
    @Test void differentSequenceValuesProduceDifferentReferences() {
        LocalDate date = LocalDate.of(2026, 9, 6);
        assertNotEquals(AppointmentNumberGenerator.generate(date, 1), AppointmentNumberGenerator.generate(date, 2));
    }
    @Test void sequenceBeyondFiveDigitsIsNotTruncated() {
        assertEquals("APT-2026-100000", AppointmentNumberGenerator.generate(LocalDate.of(2026, 9, 6), 100000));
    }
    @Test void maximumSequenceFitsDatabaseColumn() {
        assertTrue(AppointmentNumberGenerator.generate(LocalDate.of(2026, 9, 6), Long.MAX_VALUE).length() <= 32);
    }
    @Test void rejectsMissingDate() {
        assertThrows(IllegalArgumentException.class, () -> AppointmentNumberGenerator.generate(null, 1));
    }
    @Test void rejectsNonpositiveSequence() {
        assertThrows(IllegalArgumentException.class, () -> AppointmentNumberGenerator.generate(LocalDate.of(2026, 9, 6), 0));
    }
}

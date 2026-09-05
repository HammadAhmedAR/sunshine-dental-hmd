package com.sunrise.clinic.service;

import com.sunrise.clinic.model.PatientDraft;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class PatientServiceTest {
    private final PatientService service = new PatientService(null, null);

    @Test void normalisesLocalNumberAndTrimsFields() {
        assertEquals(new PatientDraft("Nimal Perera", "12 Lake Road", "+94771234567"),
                service.validate(new PatientDraft(" Nimal Perera ", " 12 Lake Road ", "077-123 4567")));
    }
    @Test void acceptsInternationalLandline() {
        assertEquals("+94112345678", service.validate(new PatientDraft("Nimal Perera", "12 Lake Road", "+94112345678")).phone());
    }
    @ParameterizedTest @ValueSource(strings = {"", "A", "123", "<script>"})
    void rejectsInvalidNames(String name) {
        assertThrows(ValidationException.class, () -> service.validate(new PatientDraft(name, "12 Lake Road", "0771234567")));
    }
    @Test void rejectsMissingAddress() {
        assertThrows(ValidationException.class, () -> service.validate(new PatientDraft("Nimal Perera", null, "0771234567")));
    }
    @ParameterizedTest @ValueSource(strings = {"", "123", "+44771234567", "0001234567", "07712345678"})
    void rejectsInvalidNumbers(String phone) {
        assertThrows(ValidationException.class, () -> service.validate(new PatientDraft("Nimal Perera", "12 Lake Road", phone)));
    }
}

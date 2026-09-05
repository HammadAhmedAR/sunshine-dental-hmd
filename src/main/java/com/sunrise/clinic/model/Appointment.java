package com.sunrise.clinic.model;

import java.time.Instant;

public record Appointment(long id, String appointmentNumber, long patientId, long dentistId,
                          long treatmentId, long createdBy, Instant startsAt, Instant endsAt) { }

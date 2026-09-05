package com.sunrise.clinic.model;

/** Raw form values are parsed and validated by the service, not the servlet. */
public record AppointmentRequest(String existingPatientId, PatientDraft patient,
                                 String dentistId, String treatmentId, String date, String time) { }

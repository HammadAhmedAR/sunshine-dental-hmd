package com.sunrise.clinic.model;

import java.util.List;

public record DailySchedule(List<AppointmentDetails> appointments, long completed, long cancelled, String popularTreatment) {
    public List<AppointmentDetails> getAppointments() { return appointments; }
    public int getTotal() { return appointments.size(); }
    public long getCompleted() { return completed; }
    public long getCancelled() { return cancelled; }
    public String getPopularTreatment() { return popularTreatment; }
}

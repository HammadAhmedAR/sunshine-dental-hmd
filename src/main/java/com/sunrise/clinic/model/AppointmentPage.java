package com.sunrise.clinic.model;

import java.util.List;

public record AppointmentPage(List<AppointmentDetails> appointments, int page, boolean hasNext) {
    public List<AppointmentDetails> getAppointments() { return appointments; }
    public int getPage() { return page; }
    public boolean isHasNext() { return hasNext; }
}

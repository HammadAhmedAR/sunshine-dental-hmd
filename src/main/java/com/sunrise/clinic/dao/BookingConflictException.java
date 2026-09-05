package com.sunrise.clinic.dao;

import java.sql.SQLException;

/** Maps only the known dentist-slot uniqueness constraint to a business conflict. */
public final class BookingConflictException extends SQLException {
    public BookingConflictException(SQLException cause) { super("Dentist slot conflict.", "23505", cause); }
}

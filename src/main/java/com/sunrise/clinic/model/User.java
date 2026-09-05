package com.sunrise.clinic.model;

import java.io.Serializable;

/** Safe session identity: deliberately contains no password or password hash. */
public record User(long id, String username, String fullName, String role) implements Serializable {
    public long getId() { return id; }
    public String getFullName() { return fullName; }
}

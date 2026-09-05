package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.DentistDAO;
import com.sunrise.clinic.model.Dentist;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.util.List;

public final class DentistService {
    private final ConnectionProvider connections;
    private final DentistDAO dentists;
    public DentistService(ConnectionProvider connections, DentistDAO dentists) {
        this.connections = connections; this.dentists = dentists;
    }
    public List<Dentist> listActive() throws SQLException {
        try (Connection connection = connections.getConnection()) { return dentists.findActive(connection); }
    }
}

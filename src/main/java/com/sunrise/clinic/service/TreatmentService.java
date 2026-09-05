package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.TreatmentDAO;
import com.sunrise.clinic.model.Treatment;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.util.List;

public final class TreatmentService {
    private final ConnectionProvider connections;
    private final TreatmentDAO treatments;
    public TreatmentService(ConnectionProvider connections, TreatmentDAO treatments) {
        this.connections = connections; this.treatments = treatments;
    }
    public List<Treatment> listActive() throws SQLException {
        try (Connection connection = connections.getConnection()) { return treatments.findActive(connection); }
    }
}

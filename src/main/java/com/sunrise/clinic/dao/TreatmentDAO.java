package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.Treatment;
import java.sql.*;
import java.util.*;

public interface TreatmentDAO {
    List<Treatment> findActive(Connection connection) throws SQLException;
    Optional<Treatment> findActiveById(Connection connection, long id) throws SQLException;
}

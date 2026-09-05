package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.Dentist;
import java.sql.*;
import java.util.*;

public interface DentistDAO {
    List<Dentist> findActive(Connection connection) throws SQLException;
    Optional<Dentist> lockActive(Connection connection, long id) throws SQLException;
}

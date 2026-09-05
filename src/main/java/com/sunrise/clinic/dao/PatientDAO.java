package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.*;
import java.sql.*;
import java.util.*;

public interface PatientDAO {
    List<Patient> findAll(Connection connection) throws SQLException;
    Optional<Patient> findById(Connection connection, long id) throws SQLException;
    long insert(Connection connection, PatientDraft patient) throws SQLException;
}

import com.sunrise.clinic.config.DatabaseConfig;
import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.service.*;
import com.sunrise.clinic.util.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manual integration check for the isolated Phase 2 fixture only.
 * Requires the fresh schema/seed plus the HTTP smoke fixture (one patient, two appointments).
 * Never point this at a real clinic database. It intentionally creates fictional appointments.
 */
class Phase2DatabaseCheck {
    static final ConnectionProvider CONNECTIONS = () -> {
        try { return new DBConnection(DatabaseConfig.load()).getConnection(); }
        catch (Exception e) { throw new SQLException("Test configuration unavailable.", e); }
    };
    static final Clock CLOCK = Clock.systemUTC();
    static final LocalDate DATE = LocalDate.now(DashboardService.CLINIC_ZONE).plusDays(3);
    static final JdbcAppointmentDAO DAO = new JdbcAppointmentDAO();

    static AppointmentService service(AppointmentDAO dao) {
        return new AppointmentService(CONNECTIONS, new JdbcPatientDAO(), new JdbcDentistDAO(),
                new JdbcTreatmentDAO(), dao, new PatientService(CONNECTIONS, new JdbcPatientDAO()), CLOCK);
    }
    static AppointmentRequest request(String name, String time) {
        return new AppointmentRequest("", new PatientDraft(name, "12 Integration Test Road", "0771234567"),
                "1", "1", DATE.toString(), time);
    }
    static long count(String table) throws Exception {
        if (!Set.of("patients", "appointments", "users", "dentists", "treatments").contains(table)) throw new IllegalArgumentException();
        try (Connection c = CONNECTIONS.getConnection(); Statement s = c.createStatement();
             ResultSet rows = s.executeQuery("SELECT count(*) FROM " + table)) {
            rows.next(); return rows.getLong(1);
        }
    }
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
    public static void main(String[] args) throws Exception {
        check(count("users") == 1 && count("dentists") == 2 && count("treatments") == 5, "Seed rerun preserves reference counts");
        long patientsBefore = count("patients"), appointmentsBefore = count("appointments");
        check(patientsBefore == 1 && appointmentsBefore == 2, "HTTP conflict/reuse fixture has one patient and two appointments");

        AppointmentDAO failAfterBothInserts = new AppointmentDAO() {
            public Optional<AppointmentDetails> findByReference(Connection c, String n, boolean lock) throws SQLException { return DAO.findByReference(c,n,lock); }
            public List<AppointmentDetails> list(Connection c, LocalDate d, AppointmentStatus s, int limit, int offset) throws SQLException { return DAO.list(c,d,s,limit,offset); }
            public boolean hasOverlapExcluding(Connection c, long id, Instant start, Instant end, long excluded) throws SQLException { return DAO.hasOverlapExcluding(c,id,start,end,excluded); }
            public void reschedule(Connection c, long id, long d, long t, Instant start, Instant end) throws SQLException { DAO.reschedule(c,id,d,t,start,end); }
            public void changeStatus(Connection c, long id, AppointmentStatus s) throws SQLException { DAO.changeStatus(c,id,s); }
            public boolean hasOverlap(Connection c, long id, Instant start, Instant end) throws SQLException {
                return DAO.hasOverlap(c, id, start, end);
            }
            public Appointment insert(Connection c, long p, long d, long t, long u, Instant start, Instant end) throws SQLException {
                DAO.insert(c, p, d, t, u, start, end);
                throw new SQLException("Deliberate integration-test failure after both inserts.");
            }
        };
        try {
            service(failAfterBothInserts).register(request("Rollback Test Patient", "12:00"), 1);
            throw new AssertionError("Expected injected failure");
        } catch (SQLException expected) { }
        check(count("patients") == patientsBefore && count("appointments") == appointmentsBefore,
                "Real JDBC rollback removes both inserted patient and appointment");

        CountDownLatch ready = new CountDownLatch(2), go = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Object>> results = new ArrayList<>();
        try {
            for (String name : List.of("Concurrent First Patient", "Concurrent Second Patient")) {
                results.add(executor.submit(() -> {
                    ready.countDown(); go.await();
                    try { return service(DAO).register(request(name, "13:00"), 1); }
                    catch (ValidationException conflict) { return conflict; }
                }));
            }
            if (!ready.await(10, TimeUnit.SECONDS)) throw new AssertionError("Workers not ready");
            go.countDown();
            int wins = 0, conflicts = 0;
            Appointment saved = null;
            for (Future<Object> result : results) {
                Object value = result.get(20, TimeUnit.SECONDS);
                if (value instanceof Appointment appointment) { wins++; saved = appointment; }
                if (value instanceof ValidationException) conflicts++;
            }
            check(wins == 1 && conflicts == 1, "Concurrent same-dentist bookings yield exactly one success and one conflict");
            check(count("patients") == patientsBefore + 1 && count("appointments") == appointmentsBefore + 1,
                    "Concurrent loser leaves no orphan patient");
            check(saved.appointmentNumber().matches("APT-[0-9]{4}-[0-9]{5,}"), "Database reference has APT year/sequence format");

            try {
                service(DAO).register(request("Overlap Test Patient", "13:10"), 1);
                throw new AssertionError("Expected overlap rejection");
            } catch (ValidationException expected) { }
            check(count("patients") == patientsBefore + 1, "Partial overlap is rejected without a patient insert");

            Appointment adjacent = service(DAO).register(request("Adjacent Test Patient", "13:20"), 1);
            check(!saved.appointmentNumber().equals(adjacent.appointmentNumber()),
                    "Adjacent slot succeeds with a different database-generated reference");

            try (Connection connection = CONNECTIONS.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    DAO.insert(connection, saved.patientId(), 1, 1, 1, saved.startsAt(), saved.endsAt());
                    throw new AssertionError("Expected unique slot constraint");
                } catch (BookingConflictException expected) {
                    connection.rollback();
                    check(true, "Database uniqueness independently rejects a duplicate start");
                }
            }
        } finally {
            go.countDown();
            executor.shutdownNow();
        }
    }
}

import com.sunrise.clinic.config.DatabaseConfig;
import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.service.*;
import com.sunrise.clinic.util.*;
import java.sql.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

/** Optional integration checks. Requires only the isolated local fictional fixture, not Maven tests. */
class Phase3DatabaseCheck {
    static final ConnectionProvider CP = () -> {
        try { return new DBConnection(DatabaseConfig.load()).getConnection(); }
        catch (Exception e) { throw new SQLException("Test configuration unavailable.",e); }
    };
    static final JdbcAppointmentDAO APPOINTMENTS = new JdbcAppointmentDAO();
    static final Clock NOW = Clock.systemUTC();
    static final ZoneId ZONE = ZoneId.of("Asia/Colombo");
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
    static Appointment create(String name, LocalDate date, String time, Clock clock) throws Exception {
        AppointmentService service = new AppointmentService(CP,new JdbcPatientDAO(),new JdbcDentistDAO(),
            new JdbcTreatmentDAO(),APPOINTMENTS,new PatientService(CP,new JdbcPatientDAO()),clock);
        return service.register(new AppointmentRequest("",new PatientDraft(name,"24 Fictional Lotus Lane","0771234567"),
            "2","2",date.toString(),time),1);
    }
    public static void main(String[] args) throws Exception {
        try (Connection c = CP.getConnection()) {
            check(c.getMetaData().getURL().startsWith("jdbc:postgresql://127.0.0.1:55432/"),
                "Guard: isolated local PostgreSQL port");
        }
        LocalDate date = LocalDate.now(ZONE).plusDays(30);
        try (Connection c = CP.getConnection()) {
            while (!APPOINTMENTS.list(c,date,null,1,0).isEmpty()) date = date.plusDays(1);
        }
        Appointment appointment = create("Sanduni Abeysekera",date,"09:00",NOW);
        BillService billing = new BillService(CP,APPOINTMENTS,new JdbcBillDAO(),NOW);
        CountDownLatch ready = new CountDownLatch(2), go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Object>> results = new ArrayList<>();
        Bill saved = null;
        try {
            for (int i=0;i<2;i++) results.add(pool.submit(() -> {
                ready.countDown(); go.await();
                try { return billing.create(appointment.appointmentNumber(),1); }
                catch (ValidationException e) { return e; }
            }));
            check(ready.await(10,TimeUnit.SECONDS),"Concurrent bill workers ready");
            go.countDown();
            int wins=0, rejected=0;
            for (Future<Object> future : results) {
                Object value=future.get(20,TimeUnit.SECONDS);
                if (value instanceof Bill bill) { saved=bill; wins++; }
                if (value instanceof ValidationException) rejected++;
            }
            check(wins==1 && rejected==1,"Concurrent billing produces one success and one rejection");
        } finally { go.countDown(); pool.shutdownNow(); }
        try (Connection c = CP.getConnection()) {
            try (PreparedStatement s = c.prepareStatement("SELECT count(*) FROM bills WHERE appointment_id=?")) {
                s.setLong(1,appointment.id());
                try (ResultSet r=s.executeQuery()) { r.next(); check(r.getInt(1)==1,"Database contains one final bill"); }
            }
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s=c.prepareStatement("UPDATE treatments SET price=price+123,name=name || ' demo' WHERE treatment_id=?")) {
                    s.setLong(1,appointment.treatmentId()); s.executeUpdate();
                }
                Bill reread = new JdbcBillDAO().find(c,saved.billNumber()).orElseThrow();
                check(reread.total().compareTo(saved.total())==0 && reread.treatmentName().equals(saved.treatmentName()),
                    "Receipt snapshots survive changed treatment price/name");
            } finally { c.rollback(); }
        }
        AppointmentManagementService manage=new AppointmentManagementService(CP,APPOINTMENTS,new JdbcDentistDAO(),new JdbcTreatmentDAO(),NOW);
        // A historical test clock creates fictional past visits through the normal registration service.
        LocalDate past = LocalDate.now(ZONE).minusDays(1);
        try (Connection c = CP.getConnection()) {
            while (!APPOINTMENTS.list(c,past,null,1,0).isEmpty()) past=past.minusDays(1);
        }
        Clock pastClock=Clock.fixed(past.minusDays(1).atStartOfDay(ZONE).toInstant(),ZoneOffset.UTC);
        Appointment completed=create("Dinesh Rathnayake",past,"09:00",pastClock);
        Appointment noShow=create("Ishara Karunaratne",past,"11:00",pastClock);
        manage.changeStatus(completed.appointmentNumber(),"COMPLETED");
        manage.changeStatus(noShow.appointmentNumber(),"NO_SHOW");
        AppointmentQueryService queries=new AppointmentQueryService(CP,APPOINTMENTS);
        check(queries.find(completed.appointmentNumber()).status()==AppointmentStatus.COMPLETED,"Past appointment completes");
        check(queries.find(noShow.appointmentNumber()).status()==AppointmentStatus.NO_SHOW,"Past appointment becomes no-show");
        try { billing.create(noShow.appointmentNumber(),1); throw new AssertionError("No-show billed"); }
        catch (ValidationException expected) { check(true,"No-show cannot be billed"); }
        Bill completedBill=billing.create(completed.appointmentNumber(),1);
        check(completedBill.total().signum()>0,"Completed appointment can be billed");
        try (Connection c=CP.getConnection(); PreparedStatement s=c.prepareStatement(
                "SELECT appointment_number FROM appointments WHERE appointment_number LIKE 'SDC-%' ORDER BY appointment_id LIMIT 1");
             ResultSet r=s.executeQuery()) {
            check(r.next(),"Legacy reference fixture preserved");
            String legacy=r.getString(1);
            check(queries.find(" " + legacy + " ").appointmentNumber().equals(legacy),"Legacy exact lookup works");
            System.out.println("DEMO: legacy=" + legacy);
        }
        String today=LocalDate.now(ZONE).toString();
        RevenueSummary summary=new ReportService(CP,new JdbcReportDAO()).revenue(today,today);
        try (Connection c=CP.getConnection(); PreparedStatement s=c.prepareStatement(
                "SELECT count(*),coalesce(sum(total),0) FROM bills WHERE (issued_at AT TIME ZONE 'Asia/Colombo')::date=?")) {
            s.setObject(1,LocalDate.parse(today));
            try (ResultSet r=s.executeQuery()) {
                r.next();
                check(summary.billCount()==r.getLong(1) && summary.totalRevenue().compareTo(r.getBigDecimal(2))==0,
                    "View-backed revenue matches independently summed stored bills");
            }
        }
        RevenueSummary empty=new ReportService(CP,new JdbcReportDAO()).revenue("2000-01-01","2000-01-01");
        check(empty.billCount()==0 && empty.totalRevenue().compareTo(BigDecimal.ZERO)==0,"Empty database report returns zero totals");
        System.out.println("DEMO: completed=" + completed.appointmentNumber() + " no-show=" + noShow.appointmentNumber()
            + " date=" + past + " concurrent-bill=" + saved.billNumber());
    }
}

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>New appointment | Sunrise Dental Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <script src="${pageContext.request.contextPath}/assets/js/appointment.js" defer></script>
</head>
<body>
<div class="app-shell">
    <%@ include file="../fragments/sidebar.jspf" %>
    <div class="workspace">
        <%@ include file="../fragments/topbar.jspf" %>
        <main id="main-content" class="content">
            <span class="eyebrow">PATIENT VISITS</span>
            <h1 class="page-title">Register an appointment</h1>
            <p class="page-intro">Choose a patient, dentist and treatment. Dates and times use Sri Lanka time.</p>
            <c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>
            <c:if test="${not referenceUnavailable}">
                <form class="card" method="post" action="${pageContext.request.contextPath}/appointments/new">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <div class="form-group"><label for="appointmentNumber">Appointment number</label><input class="form-control" id="appointmentNumber" value="Assigned automatically when saved" readonly><p class="form-hint">The unique APT reference will appear in your confirmation.</p></div>
                    <fieldset class="form-section">
                        <legend>01 &nbsp; Patient details</legend>
                        <div class="form-group">
                            <label for="existingPatientId">Patient</label>
                            <select class="form-control" id="existingPatientId" name="existingPatientId" aria-describedby="patient-hint">
                                <option value="">Register a new patient</option>
                                <c:forEach items="${patients}" var="patient">
                                    <option value="${patient.id}" ${param.existingPatientId == patient.idValue ? 'selected' : ''} data-address="<c:out value='${patient.address}'/>" data-phone="<c:out value='${patient.phone}'/>"><c:out value="${patient.fullName}"/> — <c:out value="${patient.phone}"/> (ID <c:out value="${patient.id}"/>)</option>
                                </c:forEach>
                            </select>
                            <p class="form-hint" id="patient-hint">Select an existing patient to reuse their saved details, or enter all three fields below for a new patient.</p>
                            <p class="form-hint" id="selected-patient" aria-live="polite"></p>
                        </div>
                        <div class="form-grid" id="new-patient-fields">
                            <div class="form-group form-wide"><label for="fullName">Patient name (new patient)</label><input class="form-control" id="fullName" name="fullName" maxlength="120" autocomplete="name" value="<c:out value='${param.fullName}'/>"></div>
                            <div class="form-group form-wide"><label for="address">Address (new patient)</label><textarea class="form-control" id="address" name="address" maxlength="300" rows="3" autocomplete="street-address"><c:out value="${param.address}"/></textarea></div>
                            <div class="form-group"><label for="phone">Contact number (new patient)</label><input class="form-control" type="tel" id="phone" name="phone" maxlength="20" autocomplete="tel" placeholder="0771234567" value="<c:out value='${param.phone}'/>"><p class="form-hint">Local 10-digit or +94 format.</p></div>
                        </div>
                    </fieldset>
                    <fieldset class="form-section">
                        <legend>02 &nbsp; Visit details</legend>
                        <div class="form-grid">
                            <div class="form-group"><label for="dentistId">Dentist</label><select class="form-control" id="dentistId" name="dentistId" required><option value="">Select a dentist</option><c:forEach items="${dentists}" var="dentist"><option value="${dentist.id}" ${param.dentistId == dentist.idValue ? 'selected' : ''}><c:out value="${dentist.fullName}"/></option></c:forEach></select></div>
                            <div class="form-group"><label for="treatmentId">Treatment</label><select class="form-control" id="treatmentId" name="treatmentId" required><option value="">Select a treatment</option><c:forEach items="${treatments}" var="treatment"><option value="${treatment.id}" ${param.treatmentId == treatment.idValue ? 'selected' : ''}><c:out value="${treatment.name}"/> · LKR <c:out value="${treatment.price}"/> · <c:out value="${treatment.durationMinutes}"/> min</option></c:forEach></select></div>
                            <div class="form-group"><label for="date">Appointment date</label><input class="form-control" type="date" id="date" name="date" required value="<c:out value='${param.date}'/>"></div>
                            <div class="form-group"><label for="time">Appointment time</label><input class="form-control" type="time" id="time" name="time" step="60" required value="<c:out value='${param.time}'/>"></div>
                        </div>
                        <p class="form-hint">The treatment duration determines the appointment end time. Overlapping dentist visits cannot be booked.</p>
                    </fieldset>
                    <div class="form-actions"><button class="button button-primary" type="submit">Register appointment</button><a href="${pageContext.request.contextPath}/dashboard">Cancel</a></div>
                </form>
            </c:if>
        </main>
    </div>
</div>
</body>
</html>


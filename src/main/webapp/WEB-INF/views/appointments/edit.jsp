<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Reschedule appointment | Sunrise Dental Clinic</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body><div class="app-shell"><%@ include file="../fragments/sidebar.jspf" %><div class="workspace"><%@ include file="../fragments/topbar.jspf" %>
<main id="main-content" class="content"><h1 class="page-title">Reschedule appointment</h1>
<c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>

<form class="card" method="post" action="${pageContext.request.contextPath}/appointments/edit"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="number" value="<c:out value='${appointment.appointmentNumber}'/>">
<p><strong><c:out value="${appointment.appointmentNumber}"/></strong> · <c:out value="${appointment.patientName}"/></p><p class="form-hint">Patient details and appointment number stay unchanged.</p>
<div class="form-grid">
<div class="form-group"><label for="dentistId">Dentist</label><select class="form-control" id="dentistId" name="dentistId" required><option value="">Select dentist</option><c:forEach items="${dentists}" var="d"><option value="${d.id}" ${formDentist == d.idValue ? 'selected' : ''}><c:out value="${d.fullName}"/></option></c:forEach></select></div>
<div class="form-group"><label for="treatmentId">Treatment</label><select class="form-control" id="treatmentId" name="treatmentId" required><option value="">Select treatment</option><c:forEach items="${treatments}" var="t"><option value="${t.id}" ${formTreatment == t.idValue ? 'selected' : ''}><c:out value="${t.name}"/> · LKR <c:out value="${t.price}"/></option></c:forEach></select></div>
<div class="form-group"><label for="date">Date</label><input class="form-control" type="date" name="date" id="date" required value="<c:out value='${formDate}'/>"></div>
<div class="form-group"><label for="time">Time</label><input class="form-control" type="time" name="time" id="time" required value="<c:out value='${formTime}'/>"></div>
</div><button class="button button-primary">Save reschedule</button></form>
</main></div></div></body></html>

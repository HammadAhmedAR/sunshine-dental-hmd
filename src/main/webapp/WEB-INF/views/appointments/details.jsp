<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Appointment details | Sunrise Dental Clinic</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body><div class="app-shell"><%@ include file="../fragments/sidebar.jspf" %><div class="workspace"><%@ include file="../fragments/topbar.jspf" %>
<main id="main-content" class="content"><h1 class="page-title">Appointment details</h1>
<c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>

<section class="card">
<div class="section-heading"><h2><c:out value="${appointment.appointmentNumber}"/></h2><span class="badge"><c:out value="${appointment.status}"/></span></div>
<dl class="details-grid"><div><dt>Patient</dt><dd><c:out value="${appointment.patientName}"/></dd></div><div><dt>Contact</dt><dd><c:out value="${appointment.phone}"/></dd></div><div><dt>Address</dt><dd><c:out value="${appointment.address}" default="Not recorded"/></dd></div><div><dt>Dentist</dt><dd><c:out value="${appointment.dentistName}"/></dd></div><div><dt>Treatment</dt><dd><c:out value="${appointment.treatmentName}"/></dd></div><div><dt>Current treatment fee</dt><dd>LKR <c:out value="${appointment.treatmentFee}"/></dd></div><div><dt>Visit (Sri Lanka time)</dt><dd><c:out value="${appointment.date}"/> at <c:out value="${appointment.time}"/></dd></div><div><dt>Registered</dt><dd><c:out value="${appointment.createdAt}"/></dd></div></dl>
<div class="form-actions"><a href="${pageContext.request.contextPath}/appointments">Back to appointments</a>
<c:if test="${appointment.editable}"><c:url var="editUrl" value="/appointments/edit"><c:param name="number" value="${appointment.appointmentNumber}"/></c:url><a class="button button-primary" href="<c:out value='${editUrl}'/>">Reschedule</a></c:if></div>
</section>
<c:if test="${appointment.status == 'BOOKED'}"><section class="card"><h2>Update appointment status</h2><p>Completed, cancelled and no-show statuses are final. No records are deleted.</p>
<form class="filter-row" method="post" action="${pageContext.request.contextPath}/appointments/status"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="number" value="<c:out value='${appointment.appointmentNumber}'/>"><div class="form-group"><label for="newStatus">New status</label><select class="form-control" name="status" id="newStatus" required><option value="">Select status</option><option>COMPLETED</option><c:if test="${not appointment.billed}"><option>CANCELLED</option><option>NO_SHOW</option></c:if></select></div><button class="button button-primary">Update status</button></form></section></c:if>
<section class="card"><h2>Billing</h2><c:choose><c:when test="${appointment.billed}"><c:url var="billUrl" value="/billing/receipt"><c:param name="appointment" value="${appointment.appointmentNumber}"/></c:url><a class="button button-primary" href="<c:out value='${billUrl}'/>">View receipt</a></c:when><c:when test="${appointment.status == 'BOOKED' or appointment.status == 'COMPLETED'}"><c:url var="billUrl" value="/billing/generate"><c:param name="number" value="${appointment.appointmentNumber}"/></c:url><a class="button button-primary" href="<c:out value='${billUrl}'/>">Generate bill</a></c:when><c:otherwise><p>Cancelled and no-show appointments cannot be billed.</p></c:otherwise></c:choose></section>
</main></div></div></body></html>

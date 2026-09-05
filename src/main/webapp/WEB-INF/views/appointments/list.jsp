<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Appointments | Sunrise Dental Clinic</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body><div class="app-shell"><%@ include file="../fragments/sidebar.jspf" %><div class="workspace"><%@ include file="../fragments/topbar.jspf" %>
<main id="main-content" class="content"><h1 class="page-title">Appointments</h1>
<c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>

<section class="card">
<form class="search-row" method="get" action="${pageContext.request.contextPath}/appointments/details">
<div class="form-group"><label for="number">Find an appointment by number</label><input class="form-control" name="number" id="number" maxlength="32" placeholder="APT-2026-00001 or SDC-1" required></div><button class="button button-primary">Search</button></form>
<form class="filter-row" method="get" action="${pageContext.request.contextPath}/appointments">
<div class="form-group"><label for="date">Appointment date</label><input class="form-control" id="date" type="date" name="date" value="<c:out value='${param.date}'/>"></div>
<div class="form-group"><label for="status">Status</label><select class="form-control" id="status" name="status"><option value="">All statuses</option><c:forTokens items="BOOKED,COMPLETED,CANCELLED,NO_SHOW" delims="," var="status"><option ${param.status == status ? 'selected' : ''}><c:out value="${status}"/></option></c:forTokens></select></div>
<button class="button button-primary">Apply filters</button><a href="${pageContext.request.contextPath}/appointments">Clear</a></form>
</section>
<div class="card table-wrap"><table><caption>Appointments in Sri Lanka time</caption><thead><tr><th>Number</th><th>Patient</th><th>Dentist / Treatment</th><th>Date / Time</th><th>Status</th><th>Actions</th></tr></thead><tbody>
<c:forEach items="${result.appointments}" var="a"><tr><td><c:out value="${a.appointmentNumber}"/></td><td><c:out value="${a.patientName}"/></td><td><c:out value="${a.dentistName}"/><br><small><c:out value="${a.treatmentName}"/></small></td><td><c:out value="${a.date}"/><br><c:out value="${a.time}"/></td><td><span class="badge"><c:out value="${a.status}"/></span></td><td>
<c:url var="detailsUrl" value="/appointments/details"><c:param name="number" value="${a.appointmentNumber}"/></c:url><a href="<c:out value='${detailsUrl}'/>">View</a>
<c:if test="${a.editable}"><c:url var="editUrl" value="/appointments/edit"><c:param name="number" value="${a.appointmentNumber}"/></c:url> · <a href="<c:out value='${editUrl}'/>">Reschedule</a></c:if>
</td></tr></c:forEach>
<c:if test="${empty result.appointments}"><tr><td colspan="6">No appointments match these filters.</td></tr></c:if>
</tbody></table>
<nav class="pagination" aria-label="Appointment pages">
<c:if test="${result.page > 1}"><c:url var="prevUrl" value="/appointments"><c:param name="date" value="${param.date}"/><c:param name="status" value="${param.status}"/><c:param name="page" value="${result.page - 1}"/></c:url><a href="<c:out value='${prevUrl}'/>">Previous</a></c:if>
<span>Page <c:out value="${result.page}"/></span>
<c:if test="${result.hasNext}"><c:url var="nextUrl" value="/appointments"><c:param name="date" value="${param.date}"/><c:param name="status" value="${param.status}"/><c:param name="page" value="${result.page + 1}"/></c:url><a href="<c:out value='${nextUrl}'/>">Next</a></c:if></nav></div>
</main></div></div></body></html>

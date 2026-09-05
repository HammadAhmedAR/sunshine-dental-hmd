<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Generate bill | Sunrise Dental Clinic</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body><div class="app-shell"><%@ include file="../fragments/sidebar.jspf" %><div class="workspace"><%@ include file="../fragments/topbar.jspf" %>
<main id="main-content" class="content"><h1 class="page-title">Generate bill</h1>
<c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>

<section class="card"><h2><c:out value="${preview.appointment.appointmentNumber}"/></h2><p><c:out value="${preview.appointment.patientName}"/> · <c:out value="${preview.appointment.treatmentName}"/></p>
<table><caption>Bill preview · LKR</caption><tbody><tr><th>Treatment cost</th><td><c:out value="${preview.amounts.treatmentCost}"/></td></tr><tr><th>Consultation fee</th><td><c:out value="${preview.amounts.consultationFee}"/></td></tr><tr><th>Total</th><td><strong><c:out value="${preview.amounts.total}"/></strong></td></tr></tbody></table>
<p class="form-hint">Fees are rechecked when you generate the final bill. A final bill prevents cancellation and rescheduling.</p>
<form method="post" action="${pageContext.request.contextPath}/billing/generate"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="number" value="<c:out value='${preview.appointment.appointmentNumber}'/>"><button class="button button-primary">Generate final bill</button></form></section>
</main></div></div></body></html>

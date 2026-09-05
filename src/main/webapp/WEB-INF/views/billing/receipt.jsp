<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Bill / receipt | Sunrise Dental Clinic</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/receipt.css"><script src="${pageContext.request.contextPath}/assets/js/receipt.js" defer></script></head>
<body><div class="app-shell"><%@ include file="../fragments/sidebar.jspf" %><div class="workspace"><%@ include file="../fragments/topbar.jspf" %>
<main id="main-content" class="content"><h1 class="page-title">Bill / receipt</h1>
<c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>

<article class="card receipt"><div class="section-heading"><div><h2 class="receipt-brand">Sunrise Dental Clinic</h2><p>Bill / Receipt · LKR</p></div><div><strong><c:out value="${bill.billNumber}"/></strong><p><c:out value="${bill.issuedAt}"/> (Sri Lanka time)</p></div></div>
<dl class="details-grid"><div><dt>Appointment</dt><dd><c:out value="${bill.appointmentNumber}"/></dd></div><div><dt>Patient</dt><dd><c:out value="${bill.patientName}"/></dd></div><div><dt>Dentist</dt><dd><c:out value="${bill.dentistName}"/></dd></div><div><dt>Treatment</dt><dd><c:out value="${bill.treatmentName}"/></dd></div></dl>
<table><caption>Charges (LKR)</caption><tbody><tr><th>Treatment cost</th><td><c:out value="${bill.treatmentCost}"/></td></tr><tr><th>Consultation fee</th><td><c:out value="${bill.consultationFee}"/></td></tr><c:if test="${bill.discount > 0}"><tr><th>Legacy discount</th><td>-<c:out value="${bill.discount}"/></td></tr></c:if><tr class="receipt-total"><th>Total amount</th><td>LKR <c:out value="${bill.total}"/></td></tr></tbody></table>
<p class="receipt-note">Thank you for choosing Sunrise Dental Clinic. This document records issued charges; payment collection is not recorded by this system.</p>
<div class="form-actions no-print"><button class="button button-primary" id="print-receipt" type="button">Print receipt</button><a href="${pageContext.request.contextPath}/billing">Billing history</a></div></article>
</main></div></div></body></html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Billing history | Sunrise Dental Clinic</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body><div class="app-shell"><%@ include file="../fragments/sidebar.jspf" %><div class="workspace"><%@ include file="../fragments/topbar.jspf" %>
<main id="main-content" class="content"><h1 class="page-title">Billing history</h1>
<c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>

<section class="card table-wrap"><table><caption>Issued bills · LKR</caption><thead><tr><th>Bill</th><th>Appointment</th><th>Patient / Treatment</th><th>Issued</th><th>Total</th><th>Action</th></tr></thead><tbody>
<c:forEach items="${result.bills}" var="b"><tr><td><c:out value="${b.billNumber}"/></td><td><c:out value="${b.appointmentNumber}"/></td><td><c:out value="${b.patientName}"/><br><small><c:out value="${b.treatmentName}"/></small></td><td><c:out value="${b.issuedAt}"/></td><td><c:out value="${b.total}"/></td><td><c:url var="url" value="/billing/receipt"><c:param name="number" value="${b.billNumber}"/></c:url><a href="<c:out value='${url}'/>">View receipt</a></td></tr></c:forEach>
<c:if test="${empty result.bills}"><tr><td colspan="6">No bills have been issued.</td></tr></c:if></tbody></table>
<nav class="pagination" aria-label="Billing pages"><c:if test="${result.page > 1}"><a href="?page=${result.page - 1}">Previous</a></c:if><span>Page <c:out value="${result.page}"/></span><c:if test="${result.hasNext}"><a href="?page=${result.page + 1}">Next</a></c:if></nav>
</section>
</main></div></div></body></html>

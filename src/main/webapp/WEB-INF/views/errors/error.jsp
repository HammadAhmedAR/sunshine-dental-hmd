<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Unable to complete request | Sunrise Dental Clinic</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"></head>
<body><div class="app-shell"><%@ include file="../fragments/sidebar.jspf" %><div class="workspace"><%@ include file="../fragments/topbar.jspf" %>
<main id="main-content" class="content"><h1 class="page-title">Unable to complete request</h1>
<c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>
<section class="card"><p>Please use the clinic navigation to continue.</p><a class="button button-primary" href="${pageContext.request.contextPath}/dashboard">Return to workspace</a></section>
</main></div></div></body></html>

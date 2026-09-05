<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard | Sunrise Dental Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body>
<div class="app-shell">
    <%@ include file="../fragments/sidebar.jspf" %>
    <div class="workspace">
        <%@ include file="../fragments/topbar.jspf" %>
        <main id="main-content" class="content">
            <span class="eyebrow">YOUR CLINIC DAY</span>
            <h1 class="page-title">Welcome, <c:out value="${sessionScope.loggedUser.fullName}"/></h1>
            <p class="page-intro">A clear view of your appointment schedule. All times are in Sri Lanka time.</p>
            <c:if test="${not empty success}"><div class="message message-success" role="status"><c:out value="${success}"/></div></c:if>
            <c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>
            <c:if test="${not empty stats}">
                <section class="stats-grid" aria-label="Appointment statistics">
                    <div class="card"><span class="badge">TODAY</span><h2 class="stat-value"><c:out value="${stats.today}"/></h2><p>Today's appointments</p><small>Booked and completed visits</small></div>
                    <div class="card"><span class="badge">LOOKING AHEAD</span><h2 class="stat-value"><c:out value="${stats.upcoming}"/></h2><p>Upcoming appointments</p><small>Booked visits from now, including today</small></div>
                </section>
            </c:if>
            <section class="card action-panel"><div><h2>Plan the next patient visit</h2><p>Create an appointment using the clinic's active dentists and treatments.</p></div><a class="button button-primary" href="${pageContext.request.contextPath}/appointments/new">New appointment</a></section>
        </main>
    </div>
</div>
</body>
</html>

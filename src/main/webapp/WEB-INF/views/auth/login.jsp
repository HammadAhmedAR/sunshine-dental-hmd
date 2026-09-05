<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Staff sign-in | Sunrise Dental Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<main class="login-shell">
    <section class="welcome-copy">
        <a class="eyebrow" href="${pageContext.request.contextPath}/">SUNRISE DENTAL CLINIC</a>
        <h1>Welcome back.<br>Care starts here.</h1>
        <p class="intro">Your dedicated space for patient visits and a well-organised clinic day.</p>
    </section>
    <section class="card access-card" aria-labelledby="login-title">
        <span class="badge">STAFF ACCESS</span>
        <h2 id="login-title">Sign in to your workspace</h2>
        <c:if test="${not empty error}"><div class="message message-error" role="alert"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <div class="form-group"><label for="username">Username</label><input class="form-control" id="username" name="username" maxlength="50" autocomplete="username" required></div>
            <div class="form-group"><label for="password">Password</label><input class="form-control" type="password" id="password" name="password" maxlength="72" autocomplete="current-password" required></div>
            <button class="button button-primary" type="submit">Sign in</button>
        </form>
        <p class="access-footnote">For authorised Sunrise Dental Clinic staff.</p>
    </section>
</main>
</body>
</html>

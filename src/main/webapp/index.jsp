<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Welcome | Sunrise Dental Clinic</title>
    <link rel="stylesheet" href="assets/css/app.css">
    <link rel="stylesheet" href="assets/css/auth.css">
</head>
<body>
<div class="app-shell">
    <%@ include file="WEB-INF/views/fragments/sidebar.jspf" %>
    <div class="workspace">
        <%@ include file="WEB-INF/views/fragments/topbar.jspf" %>
        <main id="main-content" class="content">
            <section class="welcome-grid" aria-labelledby="welcome-title">
                <div class="welcome-copy">
                    <span class="eyebrow">CARE STARTS WITH CONNECTION</span>
                    <h1 id="welcome-title">A brighter day.<br>A healthier smile.</h1>
                    <p class="lead">Sunrise Dental Clinic</p>
                    <p class="subtitle">Appointment &amp; Patient Management System</p>
                    <p class="intro">A dedicated workspace for your clinic team, designed to bring patient visits and everyday administration together.</p>
                    <a class="button button-primary" href="#staff-access">Staff sign-in <span aria-hidden="true">&#8599;</span></a>
                    <div class="care-note"><span class="sun-mark" aria-hidden="true">&#9728;</span> Thoughtful care. Organised days.</div>
                </div>
                <section class="card access-card" id="staff-access" aria-labelledby="access-title">
                    <div class="access-icon" aria-hidden="true">S<span>+</span></div>
                    <span class="badge">PROJECT FOUNDATION</span>
                    <h2 id="access-title">Your clinic workspace</h2>
                    <p>Staff access will be available in the authentication milestone.</p>
                    <div class="message message-info">Phase 1 establishes the application structure. Sign-in is not available yet.</div>
                    <button class="button button-primary" type="button" disabled>Sign-in coming soon</button>
                    <p class="access-footnote">For authorised Sunrise Dental Clinic staff.</p>
                </section>
            </section>
            <section class="foundation-strip" aria-label="Planned workspace features">
                <div><span class="feature-number">01</span><h2>Patient visits</h2><p>Appointment planning</p></div>
                <div><span class="feature-number">02</span><h2>Clear billing</h2><p>Treatment costs in LKR</p></div>
                <div><span class="feature-number">03</span><h2>Clinic insights</h2><p>Useful operational reports</p></div>
                <p class="planned-note">Planned for later milestones</p>
            </section>
            <footer>Sunrise Dental Clinic <span>Care, with clarity.</span></footer>
        </main>
    </div>
</div>
</body>
</html>

# Creates fictional appointments only in the project's isolated local fixture.
# Dates are discovered from the API to avoid replacing existing appointments on reruns.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$base = 'http://127.0.0.1:19080/sunrise-dental-clinic'
$handler = New-Object System.Net.Http.HttpClientHandler
$handler.AllowAutoRedirect = $false
$handler.CookieContainer = New-Object System.Net.CookieContainer
$client = New-Object System.Net.Http.HttpClient($handler)
function Send([string]$path, [hashtable]$fields) {
    if ($null -eq $fields) { $r = $client.GetAsync($base + $path).GetAwaiter().GetResult() }
    else {
        $pairs = New-Object 'System.Collections.Generic.Dictionary[string,string]'
        foreach ($key in $fields.Keys) { $pairs.Add($key,[string]$fields[$key]) }
        $body = New-Object System.Net.Http.FormUrlEncodedContent($pairs)
        $r = $client.PostAsync($base + $path,$body).GetAwaiter().GetResult()
    }
    return @{ Status=[int]$r.StatusCode; Body=$r.Content.ReadAsStringAsync().GetAwaiter().GetResult(); Response=$r }
}
function Check([bool]$condition,[string]$label) {
    if (!$condition) { throw "FAIL: $label" }
    Write-Output "PASS: $label"
}
function Token($page) { [regex]::Match($page.Body,'name="csrfToken" value="([^"]+)"').Groups[1].Value }
function NewVisit([string]$name,[string]$time,[string]$dentist,[string]$treatment) {
    $form = Send '/appointments/new' $null
    $fields = @{csrfToken=(Token $form); existingPatientId=''; fullName=$name; address='24 Lotus Lane, Colombo'; phone='0771234567'; dentistId=$dentist; treatmentId=$treatment; date=$script:date; time=$time}
    $result = Send '/appointments/new' $fields
    if ($result.Status -ne 302) { throw "Visit creation failed: $($result.Status)" }
    $dashboard = Send '/dashboard' $null
    $ref = [regex]::Match($dashboard.Body,'Appointment (APT-[0-9]{4}-[0-9]{5,}) registered successfully').Groups[1].Value
    if (!$ref) { throw 'Missing saved reference' }
    return $ref
}
try {
    $health = Send '/api/health' $null
    Check ($health.Status -eq 200 -and ($health.Body | ConvertFrom-Json).status -eq 'UP') 'Public health JSON'
    $landing = Send '/' $null
    Check ($landing.Status -eq 200 -and $landing.Body.Contains('Sunrise Dental Clinic')) 'Landing renders'
    Check ((Send '/dashboard' $null).Status -eq 302) 'Anonymous page redirects to login'
    $anon = Send '/api/appointments?date=2026-09-05' $null
    Check ($anon.Status -eq 401 -and ($anon.Body | ConvertFrom-Json).error -eq 'Authentication required.') 'Anonymous appointment API returns JSON 401'
    $login = Send '/login' $null
    $wrong = Send '/login' @{csrfToken=(Token $login); username='sunrise.admin'; password='incorrect'}
    Check ($wrong.Body.Contains('The username or password is incorrect.')) 'Invalid login is generic'
    $before = $handler.CookieContainer.GetCookies([Uri]$base)['JSESSIONID'].Value
    $valid = Send '/login' @{csrfToken=(Token $login); username='sunrise.admin'; password='SunriseLocal!2026'}
    Check ($valid.Status -eq 302) 'BCrypt login succeeds'
    Check ($before -ne $handler.CookieContainer.GetCookies([Uri]$base)['JSESSIONID'].Value) 'Session identity rotates'
    $dashboard = Send '/dashboard' $null
    Check ($dashboard.Status -eq 200 -and $dashboard.Body.Contains('Sunrise Administrator')) 'Dashboard renders live data'
    $csrf = Token $dashboard
    $day = (Get-Date).AddDays(10)
    do {
        $date = $day.ToString('yyyy-MM-dd')
        $rows = Send ("/api/appointments?date=" + $date) $null
        Check ($rows.Status -eq 200) 'Authenticated collection API responds'
        $day = $day.AddDays(1)
    } while (($rows.Body | ConvertFrom-Json).appointments.Count -gt 0)
    $a = NewVisit 'Nimal Jayasinghe' '10:00' '1' '1'
    $b = NewVisit 'Kumari Senanayake' '11:00' '1' '2'
    $c = NewVisit 'Tharindu Wijesinghe' '10:00' '2' '3'
    Check ($a -match '^APT-' -and $a -ne $b -and $a -ne $c) 'New appointments use distinct readable references, multiple dentists and treatments'
    $duplicate = Send '/appointments/new' @{csrfToken=$csrf; existingPatientId=''; fullName='Duplicate Demo Patient'; address='24 Lotus Lane'; phone='0771234567'; dentistId='1'; treatmentId='1'; date=$date; time='10:00'}
    Check ($duplicate.Status -eq 400 -and $duplicate.Body.Contains('already has an appointment')) 'Double booking rejected'
    $list = Send ("/appointments?date=" + $date + '&status=BOOKED') $null
    Check ($list.Status -eq 200 -and $list.Body.Contains($a) -and $list.Body.Contains($b)) 'Date and status filtered list renders'
    $details = Send ("/appointments/details?number=%20" + $a + '%20') $null
    Check ($details.Status -eq 200 -and $details.Body.Contains('Nimal Jayasinghe') -and $details.Body.Contains('24 Lotus Lane')) 'Trimmed exact search returns joined appointment details'
    Check ((Send '/appointments/details?number=APT-2026-999999999' $null).Status -eq 404) 'Unknown reference returns safe 404'
    Check ((Send '/appointments/details?number=' $null).Status -eq 400) 'Blank reference rejected'
    Check ((Send '/appointments?date=2026-02-30' $null).Status -eq 400) 'Invalid filter date rejected'
    $edit = @{csrfToken=$csrf;number=$a;dentistId='1';treatmentId='1';date=$date;time='10:00'}
    Check ((Send '/appointments/edit' $edit).Status -eq 302) 'Rescheduling to same slot excludes itself'
    $edit.time = '12:00'
    Check ((Send '/appointments/edit' $edit).Status -eq 302) 'Reschedule saves new time'
    $read = Send ("/api/appointments/" + $a) $null
    $json = $read.Body | ConvertFrom-Json
    Check ($read.Status -eq 200 -and $json.appointmentNumber -eq $a -and $json.time -eq '12:00') 'REST confirms unchanged reference and rescheduled time'
    $edit.number=$b; $edit.treatmentId='2'
    Check ((Send '/appointments/edit' $edit).Status -eq 400) 'Reschedule into another appointment rejected'
    Check ((Send '/appointments/status' @{csrfToken=$csrf;number=$b;status='CANCELLED'}).Status -eq 302) 'Cancellation saves controlled status'
    $cancelled = (Send ("/api/appointments/" + $b) $null).Body | ConvertFrom-Json
    Check ($cancelled.status -eq 'CANCELLED') 'Cancelled record is retained and readable'
    Check ((Send '/appointments/status' @{csrfToken=$csrf;number=$b;status='BOOKED'}).Status -eq 400) 'Terminal appointment cannot reopen'
    Check ((Send ("/billing/generate?number=" + $b) $null).Status -eq 400) 'Cancelled appointment cannot be billed'
    $preview = Send ("/billing/generate?number=" + $a) $null
    Check ($preview.Status -eq 200 -and $preview.Body.Contains('500.00')) 'Bill preview reads configured consultation fee'
    $bill = Send '/billing/generate' @{csrfToken=$csrf;number=$a;total='0.01';treatmentCost='0.01';consultationFee='0.00'}
    Check ($bill.Status -eq 302) 'Final bill persists using server amounts'
    $location = $bill.Response.Headers.Location
    $receiptPath = $(if ($location.IsAbsoluteUri) { $location.PathAndQuery } else { $location.OriginalString }).Replace('/sunrise-dental-clinic','')
    $receipt = Send $receiptPath $null
    Check ($receipt.Status -eq 200 -and $receipt.Body.Contains('LKR 2500.00') -and $receipt.Body.Contains('Print receipt')) 'Receipt shows correct 2000 + 500 total and print action'
    Check ((Send '/billing/generate' @{csrfToken=$csrf;number=$a}).Status -eq 400) 'Duplicate bill rejected'
    Check ((Send ("/appointments/edit?number=" + $a) $null).Status -eq 409) 'Billed visit cannot be rescheduled'
    $history = Send '/billing' $null
    Check ($history.Status -eq 200 -and $history.Body.Contains($a)) 'Billing history includes generated bill'
    $today = (Get-Date).ToString('yyyy-MM-dd')
    $report = Send ("/reports?date=" + $date + '&from=' + $today + '&to=' + $today) $null
    Check ($report.Status -eq 200 -and $report.Body.Contains($a) -and $report.Body.Contains('Total billed revenue')) 'Daily schedule and revenue summary render from database'
    Check ((Send '/reports?from=2026-09-05&to=2026-09-04' $null).Status -eq 400) 'Invalid report range rejected'
    $help = Send '/help' $null
    Check ($help.Status -eq 200 -and $help.Body.Contains('Printing')) 'Help renders staff workflow guidance'
    Check ((Send '/api/appointments' $null).Status -eq 400) 'API requires date for collection'
    Check ((Send '/api/appointments/APT-2026-999999999' $null).Status -eq 404) 'API returns JSON 404 for unknown reference'
    Check ((Send '/api/appointments/not-valid' $null).Status -eq 400) 'API rejects malformed reference'
    Check ((Send '/WEB-INF/views/billing/receipt.jsp' $null).Status -eq 404) 'Direct protected JSP inaccessible'
    $forged = Send '/logout' @{csrfToken='forged'}
    Check ($forged.Status -eq 403 -and !$forged.Body.Contains('GlassFish')) 'Forged POST receives safe 403'
    Check ((Send '/logout' $null).Status -eq 405) 'GET cannot log out'
    Check ((Send '/logout' @{csrfToken=$csrf}).Status -eq 302) 'POST logout succeeds'
    Check ((Send '/dashboard' $null).Status -eq 302) 'Logout invalidates access'
    Check ((Send ("/api/appointments/" + $a) $null).Status -eq 401) 'Logout invalidates API access'
    Write-Output "DEMO: date=$date booked-and-billed=$a cancelled=$b second-dentist=$c receipt=$receiptPath"
} finally { $client.Dispose() }

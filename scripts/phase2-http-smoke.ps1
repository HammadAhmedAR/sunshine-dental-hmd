# Only run against the fresh isolated Phase 2 fixture at port 19080.
# Creates fictional records and assumes patient ID 1 is free; never run against real clinic data.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$base = 'http://127.0.0.1:19080/sunrise-dental-clinic'
$handler = New-Object System.Net.Http.HttpClientHandler
$handler.AllowAutoRedirect = $false
$handler.CookieContainer = New-Object System.Net.CookieContainer
$client = New-Object System.Net.Http.HttpClient($handler)
function Send([string]$path, [hashtable]$fields) {
    if ($null -eq $fields) {
        $response = $client.GetAsync($base + $path).GetAwaiter().GetResult()
    } else {
        $pairs = New-Object 'System.Collections.Generic.Dictionary[string,string]'
        foreach ($key in $fields.Keys) { $pairs.Add($key, [string]$fields[$key]) }
        $body = New-Object System.Net.Http.FormUrlEncodedContent($pairs)
        $response = $client.PostAsync($base + $path, $body).GetAwaiter().GetResult()
    }
    return @{ Status = [int]$response.StatusCode; Body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult(); Response = $response }
}
function Check([bool]$condition, [string]$label) { if (!$condition) { throw "FAIL: $label" }; Write-Output "PASS: $label" }
function Token($page) { return [regex]::Match($page.Body, 'name="csrfToken" value="([^"]+)"').Groups[1].Value }
$health = Send '/api/health' $null
Check ($health.Status -eq 200 -and ($health.Body | ConvertFrom-Json).status -eq 'UP') 'Public health HTTP 200 JSON'
$landing = Send '/' $null
Check ($landing.Status -eq 200 -and $landing.Body.Contains('Sunrise Dental Clinic')) 'Landing JSP renders'
$anon = Send '/dashboard' $null
Check ($anon.Status -eq 302) 'Anonymous dashboard request redirects'
$private = Send '/WEB-INF/views/dashboard/dashboard.jsp' $null
Check ($private.Status -ne 200) 'Protected JSP cannot be requested directly'
$login = Send '/login' $null
Check ($login.Status -eq 200 -and (Token $login).Length -gt 20) 'Login JSP renders with CSRF token'
$beforeId = $handler.CookieContainer.GetCookies([Uri]$base)['JSESSIONID'].Value
$wrong = Send '/login' @{ username='sunrise.admin'; password='incorrect'; csrfToken=(Token $login) }
Check ($wrong.Status -eq 200 -and $wrong.Body.Contains('The username or password is incorrect.')) 'Invalid login returns generic error'
$valid = Send '/login' @{ username='sunrise.admin'; password='SunriseLocal!2026'; csrfToken=(Token $login) }
Check ($valid.Status -eq 302) 'Seed BCrypt credentials authenticate'
$afterId = $handler.CookieContainer.GetCookies([Uri]$base)['JSESSIONID'].Value
Check ($beforeId -ne $afterId) 'Login rotates session identity'
$dash = Send '/dashboard' $null
Check ($dash.Status -eq 200 -and $dash.Body.Contains('Sunrise Administrator')) 'Dashboard renders full name and live statistics'
$form = Send '/appointments/new' $null
Check ($form.Status -eq 200 -and $form.Body.Contains('Dr. Nadeesha Perera') -and $form.Body.Contains('Dental consultation')) 'Appointment JSP loads database reference data'
$date = (Get-Date).AddDays(2).ToString('yyyy-MM-dd')
$fields = @{ csrfToken=(Token $form); existingPatientId=''; fullName='Runtime Test Patient'; address='12 Test Road, Colombo'; phone='0771234567'; dentistId='1'; treatmentId='1'; date=$date; time='10:00' }
$fields.dentistId = 'invalid'
$invalid = Send '/appointments/new' $fields
Check ($invalid.Status -eq 400 -and $invalid.Body.Contains('Select an active dentist.')) 'Malformed dentist ID returns validation instead of JSP error'
$fields.dentistId = '1'
$saved = Send '/appointments/new' $fields
Check ($saved.Status -eq 302) 'New patient appointment saves and redirects'
$confirmation = Send '/dashboard' $null
Check ($confirmation.Status -eq 200 -and $confirmation.Body -match 'Appointment APT-[0-9]{4}-[0-9]{5,} registered successfully') 'Confirmation displays database-generated reference'
$duplicate = Send '/appointments/new' $fields
Check ($duplicate.Status -eq 400 -and $duplicate.Body.Contains('already has an appointment')) 'Occupied dentist slot rejected'
$fields.existingPatientId = '1'
$fields.time = '11:00'
$reused = Send '/appointments/new' $fields
Check ($reused.Status -eq 302) 'Existing patient can be reused'
$forged = Send '/logout' @{ csrfToken='forged' }
Check ($forged.Status -eq 403) 'Forged logout CSRF rejected'
$getLogout = Send '/logout' $null
Check ($getLogout.Status -eq 405) 'GET cannot log out'
$logout = Send '/logout' @{ csrfToken=(Token $form) }
Check ($logout.Status -eq 302) 'POST logout redirects to login'
$afterLogout = Send '/dashboard' $null
Check ($afterLogout.Status -eq 302) 'Logged-out session cannot access dashboard'
$client.Dispose()

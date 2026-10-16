$ErrorActionPreference = 'Stop'
$base = if ($env:SMOKE_BASE_URL) { $env:SMOKE_BASE_URL } else { 'http://localhost:28080' }
$envFile = Join-Path $PSScriptRoot '..\.env'
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^([A-Z_]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
        }
    }
}
if (-not $env:ADMIN_EMAIL -or -not $env:ADMIN_PASSWORD) { throw 'Set ADMIN_EMAIL and ADMIN_PASSWORD (or create .env).' }
function PostJson($path, $body, $token = $null) {
    $headers = @{}
    if ($token) { $headers.Authorization = "Bearer $token" }
    Invoke-RestMethod -Method Post -Uri "$base$path" -Headers $headers -ContentType 'application/json' -Body ($body | ConvertTo-Json -Compress)
}
function GetJson($path, $token = $null) {
    $headers = @{}
    if ($token) { $headers.Authorization = "Bearer $token" }
    Invoke-RestMethod -Method Get -Uri "$base$path" -Headers $headers
}
function ExpectStatus($expected, [scriptblock]$action) {
    try { & $action | Out-Null; throw "Expected HTTP $expected, got success" }
    catch {
        if (-not $_.Exception.Response) { throw }
        $actual = [int]$_.Exception.Response.StatusCode
        if ($actual -ne $expected) { throw "Expected HTTP $expected, got $actual" }
    }
}
$health = GetJson '/health'
if ($health.status -ne 'UP') { throw 'Gateway health failed' }
$front = Invoke-WebRequest -UseBasicParsing "$base/"
if ($front.StatusCode -ne 200 -or $front.Content -notmatch 'id="app"') { throw 'Frontend is unavailable' }
$script = Invoke-WebRequest -UseBasicParsing "$base/js/app.js"
if ($script.StatusCode -ne 200 -or $script.Content -notmatch "'./router.js'") { throw 'Frontend JavaScript is unavailable' }
$admin = PostJson '/api/auth/login' @{email=$env:ADMIN_EMAIL;password=$env:ADMIN_PASSWORD}
$product = PostJson '/api/products' @{name='Smoke keyboard';price=49.90;stock=5} $admin.accessToken
$email = "smoke-$([guid]::NewGuid().ToString('N'))@example.com"
$user = PostJson '/api/auth/register' @{name='Smoke User';email=$email;password='Pass12345!'}
$profile = GetJson '/api/users/me' $user.accessToken
if ($profile.EMAIL -ne $email) { throw 'Profile mismatch' }
ExpectStatus 401 { GetJson '/api/users/me' }
ExpectStatus 403 { PostJson '/api/products' @{name='Forbidden';price=1;stock=1} $user.accessToken }
ExpectStatus 400 { PostJson '/api/orders' @{productId=$product.ID;quantity=0} $user.accessToken }
$order = PostJson '/api/orders' @{productId=$product.ID;quantity=2} $user.accessToken
if ($order.STATUS -ne 'PENDING' -or [decimal]$order.TOTAL -ne 99.80) { throw 'Order mismatch' }
$other = PostJson '/api/auth/register' @{name='Other User';email="other-$([guid]::NewGuid().ToString('N'))@example.com";password='Pass12345!'}
ExpectStatus 403 { GetJson "/api/orders/$($order.ID)" $other.accessToken }
ExpectStatus 409 { PostJson '/api/orders' @{productId=$product.ID;quantity=9} $user.accessToken }
$payment = PostJson '/api/payments' @{orderId=$order.ID} $user.accessToken
if ($payment.STATUS -ne 'COMPLETED') { throw 'Payment failed' }
$payments = @(GetJson '/api/payments' $user.accessToken)
if (-not ($payments | Where-Object { $_.ID -eq $payment.ID })) { throw 'Payment history mismatch' }
$paid = GetJson "/api/orders/$($order.ID)" $user.accessToken
if ($paid.STATUS -ne 'PAID') { throw 'Order was not marked paid' }
$found = $false
for ($i=0; $i -lt 20; $i++) {
    $notifications = @(GetJson '/api/notifications' $user.accessToken)
    if ($notifications | Where-Object { $_.ORDER_ID -eq $order.ID }) { $found = $true; break }
    Start-Sleep -Milliseconds 500
}
if (-not $found) { throw 'Notification was not delivered' }
Write-Host "PASS: user=$email order=$($order.ID) payment=$($payment.ID) notification delivered"

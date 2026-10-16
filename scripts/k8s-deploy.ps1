param([ValidateSet('docker-desktop','minikube')][string]$Runtime = 'docker-desktop')
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$envPath = Join-Path $root '.env'
if (-not (Test-Path $envPath)) { throw 'Create .env from .env.example and replace its secret values.' }
$values = @{}
Get-Content $envPath | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') { $values[$Matches[1]] = $Matches[2] }
}
foreach ($key in @('JWT_SECRET','INTERNAL_KEY','ADMIN_EMAIL','ADMIN_PASSWORD','RABBIT_USER','RABBIT_PASSWORD')) {
    if (-not $values[$key] -or $values[$key].StartsWith('replace-with-')) { throw "Set $key in .env first." }
}
if ($values.JWT_SECRET.Length -lt 32) { throw 'JWT_SECRET must have at least 32 characters.' }
$services = @('auth-service','user-service','product-service','order-service','payment-service','notification-service','gateway')
Push-Location $root
try {
    foreach ($service in $services) {
        if ($Runtime -eq 'minikube') {
            minikube image build -f "$service/Dockerfile" -t "lab3/${service}:local" .
        } else {
            docker build -f "$service/Dockerfile" -t "lab3/${service}:local" .
        }
        if ($LASTEXITCODE -ne 0) { throw "Image build failed: $service" }
    }
    kubectl apply -f k8s/namespace.yaml
    if ($LASTEXITCODE -ne 0) { throw 'Namespace creation failed.' }
    kubectl -n lab3 create secret generic store-secrets --from-env-file=$envPath --dry-run=client -o yaml | kubectl apply -f -
    if ($LASTEXITCODE -ne 0) { throw 'Secret creation failed.' }
    kubectl apply -f k8s/
    if ($LASTEXITCODE -ne 0) { throw 'Manifest apply failed.' }
    foreach ($service in @('rabbitmq') + $services) {
        kubectl -n lab3 rollout status "deployment/$service" --timeout=300s
        if ($LASTEXITCODE -ne 0) { throw "Deployment did not become ready: $service" }
    }
    kubectl -n lab3 get pods,services,deployments
} finally { Pop-Location }

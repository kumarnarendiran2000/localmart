# LocalMart — Dev Startup Script
#
# Usage:
#   .\start-dev.ps1           → start Docker + all services (assumes already built)
#   .\start-dev.ps1 -Build    → run mvn clean install first, then start everything
#
# Each service opens in its own PowerShell window so you can see logs separately.
# Run this script from the localmart/ root directory.

param(
    [switch]$Build
)

$RootDir = $PSScriptRoot

# ── Step 1: Docker Infra ─────────────────────────────────────────────────────
Write-Host ""
Write-Host "[1/4] Starting Docker infra containers..." -ForegroundColor Cyan
Set-Location $RootDir

docker compose -f docker-compose.infra.yml up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker failed to start. Is Docker Desktop running?" -ForegroundColor Red
    exit 1
}

Write-Host "Docker infra is up." -ForegroundColor Green

# ── Step 2: Build (optional) ─────────────────────────────────────────────────
Write-Host ""
if ($Build) {
    Write-Host "[2/4] Building all services (mvn clean install -DskipTests)..." -ForegroundColor Cyan
    mvn clean install -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed. Fix the errors above and retry." -ForegroundColor Red
        exit 1
    }
    Write-Host "Build successful." -ForegroundColor Green
} else {
    Write-Host "[2/4] Skipping build. Run with -Build flag to rebuild first." -ForegroundColor Yellow
}

# ── Step 3: Start discovery-server ───────────────────────────────────────────
Write-Host ""
Write-Host "[3/4] Starting discovery-server (Eureka)..." -ForegroundColor Cyan

Start-Process powershell -ArgumentList "-NoExit", "-Command", `
    "Set-Location '$RootDir'; Write-Host 'discovery-server' -ForegroundColor Cyan; mvn spring-boot:run -pl discovery-server"

# Poll until Eureka health endpoint responds
Write-Host "Waiting for Eureka to be ready..." -ForegroundColor Yellow
$eurekaReady = $false

for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 2
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8761/actuator/health" -TimeoutSec 2 -ErrorAction Stop
        if ($r.StatusCode -eq 200) {
            $eurekaReady = $true
            break
        }
    } catch {}
    Write-Host "  Still waiting... ($($i * 2)s elapsed)" -ForegroundColor DarkGray
}

if (-not $eurekaReady) {
    Write-Host "Eureka did not come up within 60s. Check the discovery-server window for errors." -ForegroundColor Red
    exit 1
}

Write-Host "Eureka is ready." -ForegroundColor Green

# ── Step 4: Start remaining services ─────────────────────────────────────────
Write-Host ""
Write-Host "[4/4] Starting remaining services..." -ForegroundColor Cyan

$services = @("api-gateway", "shop-service", "user-service", "order-service")

foreach ($svc in $services) {
    Write-Host "  Starting $svc..." -ForegroundColor White
    Start-Process powershell -ArgumentList "-NoExit", "-Command", `
        "Set-Location '$RootDir'; Write-Host '$svc' -ForegroundColor Cyan; mvn spring-boot:run -pl $svc"
    Start-Sleep -Seconds 3
}

# ── Done ─────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "All services launched. Give them ~30s to finish registering with Eureka." -ForegroundColor Green
Write-Host ""
Write-Host "  Eureka Dashboard : http://localhost:8761" -ForegroundColor White
Write-Host "  API Gateway      : http://localhost:8080" -ForegroundColor White
Write-Host "  Kafka UI         : http://localhost:8090" -ForegroundColor White
Write-Host "  pgAdmin          : http://localhost:5050" -ForegroundColor White
Write-Host "  mongo-express    : http://localhost:8091" -ForegroundColor White
Write-Host ""

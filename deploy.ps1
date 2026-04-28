# zhuxian-wiki deploy script
param(
    [string]$CommitMsg = "update"
)

# fix: use Windows credential manager instead of sh
git config --global credential.helper manager
git config --global core.autocrlf false

# add git's bin and usr/bin to PATH for sh.exe
$gitExe = (Get-Command git -ErrorAction SilentlyContinue).Source | Split-Path
if ($gitExe) {
    $env:PATH = "$gitExe;$gitExe\bin;$gitExe\usr\bin;$env:PATH"
}

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

$Green = [ConsoleColor]::Green
$Yellow = [ConsoleColor]::Yellow
$Cyan = [ConsoleColor]::Cyan
$Red = [ConsoleColor]::Red
$Gray = [ConsoleColor]::Gray
$White = [ConsoleColor]::White

Write-Host "========================================" -ForegroundColor $Yellow
Write-Host "  zhuxian-wiki deploy script" -ForegroundColor $Yellow
Write-Host "========================================" -ForegroundColor $Yellow
Write-Host ""

$BackendPath = Join-Path $ScriptRoot "zhuxian-wiki-backend"
$FrontendPath = Join-Path $ScriptRoot "zhuxian-wiki-frontend"
$JarName = "zhuxian-wiki-backend-1.0.0.jar"

# step 1: git commit
Write-Host "[INFO] Step 1/5: Git commit..." -ForegroundColor $Cyan

Set-Location $BackendPath
$backendStatus = git status --porcelain
if ($backendStatus) {
    Write-Host "  backend modified, pushing..." -ForegroundColor $Gray
    git add .
    git commit -m "$CommitMsg [backend]"
    git push origin main
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] backend pushed" -ForegroundColor $Green
    } else {
        Write-Host "  [WARN] backend push failed, skipping" -ForegroundColor $Gray
    }
} else {
    Write-Host "  backend no changes" -ForegroundColor $Gray
}

Set-Location $FrontendPath
$frontendStatus = git status --porcelain
if ($frontendStatus) {
    Write-Host "  frontend modified, pushing..." -ForegroundColor $Gray
    git add .
    git commit -m "$CommitMsg [frontend]"
    git push origin main
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] frontend pushed" -ForegroundColor $Green
    } else {
        Write-Host "  [WARN] frontend push failed, skipping" -ForegroundColor $Gray
    }
} else {
    Write-Host "  frontend no changes" -ForegroundColor $Gray
}

# step 2: backend build
Write-Host ""
Write-Host "[INFO] Step 2/5: Backend Maven package..." -ForegroundColor $Cyan
Set-Location $BackendPath
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Backend build failed" -ForegroundColor $Red
    exit 1
}
$JarPath = Join-Path $BackendPath "target\$JarName"
if (-not (Test-Path $JarPath)) {
    Write-Host "[ERROR] JAR not found" -ForegroundColor $Red
    exit 1
}
Write-Host "  [OK] Backend packaged" -ForegroundColor $Green

# step 3: frontend build
Write-Host ""
Write-Host "[INFO] Step 3/5: Frontend build..." -ForegroundColor $Cyan
Set-Location $FrontendPath
npm run build --silent
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Frontend build failed" -ForegroundColor $Red
    exit 1
}
$DistPath = Join-Path $FrontendPath "dist"
if (-not (Test-Path $DistPath)) {
    Write-Host "[ERROR] dist not found" -ForegroundColor $Red
    exit 1
}
Write-Host "  [OK] Frontend built" -ForegroundColor $Green

# step 4: upload to server
$ServerIP = "47.98.240.202"
$ServerUser = "root"
$ServerPath = "/opt/zhuxian-wiki"

Write-Host ""
Write-Host "[INFO] Step 4/5: Upload to server $ServerIP..." -ForegroundColor $Cyan
Write-Host "  uploading backend JAR..." -ForegroundColor $Gray
scp $JarPath "${ServerUser}@${ServerIP}:${ServerPath}/"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] JAR upload failed" -ForegroundColor $Red
    exit 1
}
Write-Host "  uploading frontend files..." -ForegroundColor $Gray
ssh ${ServerUser}@${ServerIP} "mkdir -p $ServerPath/html" 2>$null
scp -r "$DistPath\*" "${ServerUser}@${ServerIP}:${ServerPath}/html/"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Frontend upload failed" -ForegroundColor $Red
    exit 1
}
Write-Host "  [OK] Upload done" -ForegroundColor $Green

# step 5: restart service
Write-Host ""
Write-Host "[INFO] Step 5/5: Restart service..." -ForegroundColor $Cyan
$restartCmd = "cd $ServerPath; pkill -f `"$JarName`" 2>/dev/null; sleep 2; nohup java -jar $JarName > app.log 2>&1 & sleep 3; ps aux | grep `"$JarName`" | grep -v grep"
ssh ${ServerUser}@${ServerIP} $restartCmd

Write-Host ""
Write-Host "========================================" -ForegroundColor $Green
Write-Host "  Deploy finished!" -ForegroundColor $Green
Write-Host "========================================" -ForegroundColor $Green
Write-Host ""
Write-Host "  Frontend: http://$ServerIP/wiki/" -ForegroundColor $White
Write-Host "  Admin: http://$ServerIP/admin.html" -ForegroundColor $White
Write-Host ""
$logCmd = "tail -f $ServerPath/app.log"
Write-Host "  Logs: ssh $ServerUser@$ServerIP `"$logCmd`"" -ForegroundColor $Gray

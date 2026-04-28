# 诛仙世界 Wiki 一键部署脚本
param(
    [string]$CommitMsg = "update",
    [string]$ServerIP = "47.98.240.202",
    [string]$ServerUser = "root",
    [string]$ServerPath = "/opt/zhuxian-wiki"
)

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

$BackendPath = "F:\诛仙世界wiki\zhuxian-wiki-backend"
$FrontendPath = "F:\诛仙世界wiki\zhuxian-wiki-frontend"
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
    Write-Host "  [OK] backend pushed" -ForegroundColor $Green
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
    Write-Host "  [OK] frontend pushed" -ForegroundColor $Green
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
$JarPath = "$BackendPath\target\$JarName"
if (-not (Test-Path $JarPath)) {
    Write-Host "[ERROR] JAR not found: $JarPath" -ForegroundColor $Red
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
$DistPath = "$FrontendPath\dist"
if (-not (Test-Path $DistPath)) {
    Write-Host "[ERROR] dist not found" -ForegroundColor $Red
    exit 1
}
Write-Host "  [OK] Frontend built" -ForegroundColor $Green

# step 4: upload to server
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
Write-Host "  Logs: ssh $ServerUser@$ServerIP `"tail -f $ServerPath/app.log`"" -ForegroundColor $Gray

$ErrorActionPreference = "Continue"

$dbUser = "root"
$dbPass = "123456"
$dbName = "zhuxian_wiki"
$sqlFile = "F:\诛仙世界wiki\database\crawl_entries_import.sql"

Write-Host "========================================"
Write-Host "Importing knowledge entries..."
Write-Host "========================================"
Write-Host ""

if (-not (Test-Path $sqlFile)) {
    Write-Host "[ERROR] SQL file not found"
    exit 1
}

Write-Host "[1/3] Reading SQL file..."
$sqlContent = Get-Content $sqlFile -Raw -Encoding UTF8
Write-Host "      File size: $($sqlContent.Length) chars"

Write-Host "[2/3] Executing SQL..."
$null = $sqlContent | mysql -u $dbUser -p$dbPass $dbName --default-character-set=utf8mb4 2>$null
Write-Host "      Done!"

Write-Host "[3/3] Verifying..."
Start-Sleep -Milliseconds 300
$countResult = mysql -u $dbUser -p$dbPass $dbName --default-character-set=utf8mb4 -e "SELECT COUNT(*) FROM knowledge_entry;" 2>$null | Select-Object -Skip 1
Write-Host "      Total entries: $countResult"

Write-Host ""
Write-Host "========================================"
Write-Host "Import completed!"
Write-Host "========================================"

@echo off
REM Skrypt do backupu bazy danych Cinema Paradiso
REM Użycie: backup-db.bat

echo ========================================
echo Backup bazy danych Cinema Paradiso
echo ========================================
echo.

REM Sprawdź czy kontener działa
docker ps --filter "name=cinema-db" --format "{{.Names}}" | findstr /C:"cinema-db" >nul
if errorlevel 1 (
    echo BLAD: Kontener cinema-db nie jest uruchomiony!
    echo Uruchom: docker-compose up -d
    pause
    exit /b 1
)

REM Utwórz folder backups jeśli nie istnieje
if not exist "backups" mkdir backups

REM Generuj nazwę pliku z datą i godziną (użyj PowerShell dla lepszej kompatybilności)
for /f %%i in ('powershell -Command "Get-Date -Format 'yyyyMMdd_HHmmss'"') do set timestamp=%%i
set filename=backups\cinema_paradiso_%timestamp%.sql

echo Tworzenie backupu...
echo Plik: %filename%
echo.

REM Wykonaj backup
docker exec cinema-db pg_dump -U admin cinema_paradiso > "%filename%"

if errorlevel 1 (
    echo BLAD: Nie udalo sie utworzyc backupu!
    pause
    exit /b 1
)

REM Sprawdź rozmiar pliku
for %%A in ("%filename%") do set size=%%~zA
if %size% LSS 100 (
    echo OSTRZEZENIE: Plik backupu jest bardzo maly ^(%size% bajtow^)
    echo Moze wystapil blad podczas tworzenia backupu.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Backup utworzony pomyslnie!
echo Plik: %filename%
echo Rozmiar: %size% bajtow
echo ========================================
echo.
echo Aby przywrocic backup, uzyj: restore-db.bat
echo.
pause


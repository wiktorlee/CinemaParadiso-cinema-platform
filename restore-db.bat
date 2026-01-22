@echo off
REM Skrypt do przywracania bazy danych Cinema Paradiso
REM Użycie: restore-db.bat [nazwa_pliku.sql]
REM Jeśli nie podasz nazwy pliku, zostaniesz poproszony o wybór

echo ========================================
echo Przywracanie bazy danych Cinema Paradiso
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

REM Sprawdź czy folder backups istnieje
if not exist "backups" (
    echo BLAD: Folder 'backups' nie istnieje!
    echo Najpierw utworz backup uzywajac: backup-db.bat
    pause
    exit /b 1
)

REM Jeśli podano nazwę pliku jako argument
set backup_file=%1

REM Jeśli nie podano argumentu, pokaż listę dostępnych backupów
if "%backup_file%"=="" (
    echo Dostepne backupi w folderze 'backups':
    echo.
    dir /b backups\*.sql 2>nul
    if errorlevel 1 (
        echo BLAD: Brak plikow backupu w folderze 'backups'!
        pause
        exit /b 1
    )
    echo.
    set /p backup_file="Podaj nazwe pliku backupu (lub sciezke): "
    
    REM Jeśli użytkownik nie podał pełnej ścieżki, dodaj folder backups
    echo %backup_file% | findstr /C:"backups\" >nul
    if errorlevel 1 (
        set backup_file=backups\%backup_file%
    )
)

REM Sprawdź czy plik istnieje
if not exist "%backup_file%" (
    echo BLAD: Plik '%backup_file%' nie istnieje!
    pause
    exit /b 1
)

echo.
echo UWAGA: Ta operacja nadpisze wszystkie dane w bazie!
echo Plik backupu: %backup_file%
echo.
set /p confirm="Czy na pewno chcesz kontynuowac? (TAK/NIE): "
if /i not "%confirm%"=="TAK" (
    echo Operacja anulowana.
    pause
    exit /b 0
)

echo.
echo Przywracanie bazy danych...
echo To moze chwile potrwac...
echo.

REM Przywróć backup
docker exec -i cinema-db psql -U admin cinema_paradiso < "%backup_file%"

if errorlevel 1 (
    echo.
    echo BLAD: Nie udalo sie przywrocic backupu!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Backup przywrocony pomyslnie!
echo ========================================
echo.
pause


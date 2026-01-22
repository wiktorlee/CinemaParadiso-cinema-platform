#!/bin/bash
# Skrypt do przywracania bazy danych Cinema Paradiso (Linux/Mac)
# Użycie: ./restore-db.sh [nazwa_pliku.sql]

echo "========================================"
echo "Przywracanie bazy danych Cinema Paradiso"
echo "========================================"
echo ""

# Sprawdź czy kontener działa
if ! docker ps --filter "name=cinema-db" --format "{{.Names}}" | grep -q "cinema-db"; then
    echo "BŁĄD: Kontener cinema-db nie jest uruchomiony!"
    echo "Uruchom: docker-compose up -d"
    exit 1
fi

# Sprawdź czy folder backups istnieje
if [ ! -d "backups" ]; then
    echo "BŁĄD: Folder 'backups' nie istnieje!"
    echo "Najpierw utwórz backup używając: ./backup-db.sh"
    exit 1
fi

# Jeśli podano nazwę pliku jako argument
BACKUP_FILE="$1"

# Jeśli nie podano argumentu, pokaż listę dostępnych backupów
if [ -z "$BACKUP_FILE" ]; then
    echo "Dostępne backupi w folderze 'backups':"
    echo ""
    ls -lh backups/*.sql 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "BŁĄD: Brak plików backupu w folderze 'backups'!"
        exit 1
    fi
    echo ""
    read -p "Podaj nazwę pliku backupu (lub ścieżkę): " BACKUP_FILE
    
    # Jeśli użytkownik nie podał pełnej ścieżki, dodaj folder backups
    if [[ ! "$BACKUP_FILE" =~ ^backups/ ]] && [[ ! "$BACKUP_FILE" =~ ^/ ]]; then
        BACKUP_FILE="backups/$BACKUP_FILE"
    fi
fi

# Sprawdź czy plik istnieje
if [ ! -f "$BACKUP_FILE" ]; then
    echo "BŁĄD: Plik '$BACKUP_FILE' nie istnieje!"
    exit 1
fi

echo ""
echo "UWAGA: Ta operacja nadpisze wszystkie dane w bazie!"
echo "Plik backupu: $BACKUP_FILE"
echo ""
read -p "Czy na pewno chcesz kontynuować? (TAK/NIE): " CONFIRM
if [ "$CONFIRM" != "TAK" ]; then
    echo "Operacja anulowana."
    exit 0
fi

echo ""
echo "Przywracanie bazy danych..."
echo "To może chwilę potrwać..."
echo ""

# Przywróć backup
docker exec -i cinema-db psql -U admin cinema_paradiso < "$BACKUP_FILE"

if [ $? -ne 0 ]; then
    echo ""
    echo "BŁĄD: Nie udało się przywrócić backupu!"
    exit 1
fi

echo ""
echo "========================================"
echo "Backup przywrócony pomyślnie!"
echo "========================================"
echo ""


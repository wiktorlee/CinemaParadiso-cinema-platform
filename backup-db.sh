#!/bin/bash
# Skrypt do backupu bazy danych Cinema Paradiso (Linux/Mac)
# Użycie: ./backup-db.sh

echo "========================================"
echo "Backup bazy danych Cinema Paradiso"
echo "========================================"
echo ""

# Sprawdź czy kontener działa
if ! docker ps --filter "name=cinema-db" --format "{{.Names}}" | grep -q "cinema-db"; then
    echo "BŁĄD: Kontener cinema-db nie jest uruchomiony!"
    echo "Uruchom: docker-compose up -d"
    exit 1
fi

# Utwórz folder backups jeśli nie istnieje
mkdir -p backups

# Generuj nazwę pliku z datą i godziną
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILENAME="backups/cinema_paradiso_${TIMESTAMP}.sql"

echo "Tworzenie backupu..."
echo "Plik: $FILENAME"
echo ""

# Wykonaj backup
docker exec cinema-db pg_dump -U admin cinema_paradiso > "$FILENAME"

if [ $? -ne 0 ]; then
    echo ""
    echo "BŁĄD: Nie udało się utworzyć backupu!"
    exit 1
fi

# Sprawdź rozmiar pliku
SIZE=$(stat -f%z "$FILENAME" 2>/dev/null || stat -c%s "$FILENAME" 2>/dev/null)
if [ "$SIZE" -lt 100 ]; then
    echo ""
    echo "OSTRZEŻENIE: Plik backupu jest bardzo mały ($SIZE bajtów)!"
    echo "Może wystąpił błąd podczas tworzenia backupu."
    exit 1
fi

echo ""
echo "========================================"
echo "Backup utworzony pomyślnie!"
echo "Plik: $FILENAME"
echo "Rozmiar: $SIZE bajtów"
echo "========================================"
echo ""
echo "Aby przywrócić backup, użyj: ./restore-db.sh"
echo ""


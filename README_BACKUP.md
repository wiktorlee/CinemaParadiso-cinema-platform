# Backup i Restore bazy danych Cinema Paradiso

## Szybki start

### Windows
```bash
# Utwórz backup
backup-db.bat

# Przywróć backup
restore-db.bat
```

### Linux/Mac
```bash
# Nadaj uprawnienia do wykonania
chmod +x backup-db.sh restore-db.sh

# Utwórz backup
./backup-db.sh

# Przywróć backup
./restore-db.sh
```

---

## Szczegółowa instrukcja

### 1. Tworzenie backupu

**Windows:**
```bash
backup-db.bat
```

**Linux/Mac:**
```bash
./backup-db.sh
```

**Co się dzieje:**
- Sprawdza czy kontener `cinema-db` jest uruchomiony
- Tworzy folder `backups/` jeśli nie istnieje
- Generuje plik backupu z datą i godziną: `cinema_paradiso_YYYYMMDD_HHMMSS.sql`
- Zapisuje backup w folderze `backups/`

**Przykład nazwy pliku:**
```
backups/cinema_paradiso_20241215_143022.sql
```

---

### 2. Przywracanie backupu

**Windows:**
```bash
restore-db.bat
# Lub z konkretnym plikiem:
restore-db.bat backups/cinema_paradiso_20241215_143022.sql
```

**Linux/Mac:**
```bash
./restore-db.sh
# Lub z konkretnym plikiem:
./restore-db.sh backups/cinema_paradiso_20241215_143022.sql
```

**Co się dzieje:**
- Sprawdza czy kontener `cinema-db` jest uruchomiony
- Jeśli nie podano nazwy pliku, pokazuje listę dostępnych backupów
- Pyta o potwierdzenie (TAK/NIE)
- **UWAGA:** Nadpisuje wszystkie dane w bazie!

---

## Przenoszenie danych na inny komputer

### Krok 1: Na starym komputerze

1. Utwórz backup:
   ```bash
   backup-db.bat
   ```

2. Skopiuj plik backupu na nowy komputer:
   - Przez pendrive/USB
   - Przez sieć (email, cloud, etc.)
   - Przez Git (ale **NIE** commituj plików `.sql` do repo!)

### Krok 2: Na nowym komputerze

1. Sklonuj projekt (lub skopiuj folder)

2. Uruchom Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. Poczekaj aż kontener się uruchomi (około 10-30 sekund)

4. Skopiuj plik backupu do folderu `backups/` w projekcie

5. Przywróć backup:
   ```bash
   restore-db.bat backups/nazwa_pliku.sql
   ```

6. Gotowe! Baza danych jest przywrócona.

---

## Automatyczny backup (opcjonalnie)

### Windows - Zadanie zaplanowane (Task Scheduler)

1. Otwórz **Task Scheduler** (Harmonogram zadań)

2. Utwórz nowe zadanie:
   - **Nazwa:** Cinema Paradiso Backup
   - **Wyzwalacz:** Codziennie o 2:00
   - **Akcja:** Uruchom program
   - **Program:** `C:\ścieżka\do\projektu\backup-db.bat`

### Linux/Mac - Cron

Edytuj crontab:
```bash
crontab -e
```

Dodaj linię (backup codziennie o 2:00):
```cron
0 2 * * * cd /ścieżka/do/projektu && ./backup-db.sh
```

---

## Rozwiązywanie problemów

### Problem: "Kontener cinema-db nie jest uruchomiony"

**Rozwiązanie:**
```bash
docker-compose up -d
```

### Problem: "Brak plików backupu"

**Rozwiązanie:**
- Najpierw utwórz backup używając `backup-db.bat` lub `backup-db.sh`
- Sprawdź czy folder `backups/` istnieje

### Problem: "Nie udało się przywrócić backupu"

**Możliwe przyczyny:**
1. Kontener nie jest uruchomiony
2. Plik backupu jest uszkodzony
3. Błąd w składni SQL

**Rozwiązanie:**
1. Sprawdź czy kontener działa: `docker ps`
2. Sprawdź logi: `docker logs cinema-db`
3. Spróbuj utworzyć nowy backup i przywrócić go

### Problem: "Plik backupu jest bardzo mały"

**Przyczyna:**
- Backup nie został utworzony poprawnie
- Baza danych jest pusta

**Rozwiązanie:**
- Sprawdź czy kontener działa
- Sprawdź czy baza ma dane: `docker exec cinema-db psql -U admin cinema_paradiso -c "SELECT COUNT(*) FROM users;"`

---

## Bezpieczeństwo

⚠️ **WAŻNE:**
- Pliki backupu zawierają hasła użytkowników (zahashowane)
- **NIE** commituj plików `.sql` do repozytorium Git!
- Folder `backups/` jest już dodany do `.gitignore`

---

## Przykładowe użycie

### Scenariusz: Przeniesienie projektu na laptop

1. **Na komputerze stacjonarnym:**
   ```bash
   backup-db.bat
   # Utworzy: backups/cinema_paradiso_20241215_143022.sql
   ```

2. **Skopiuj plik na laptop** (przez pendrive/email)

3. **Na laptopie:**
   ```bash
   # Sklonuj projekt
   git clone <repo-url>
   cd CinemaParadiso
   
   # Uruchom Docker
   docker-compose up -d
   
   # Skopiuj backup do folderu backups/
   # (przez eksplorator plików)
   
   # Przywróć backup
   restore-db.bat backups/cinema_paradiso_20241215_143022.sql
   ```

4. **Gotowe!** Masz wszystkie dane na laptopie.

---

## Dodatkowe informacje

### Gdzie są przechowywane backupy?

- Folder: `backups/` w głównym katalogu projektu
- Format: `cinema_paradiso_YYYYMMDD_HHMMSS.sql`

### Jak często robić backup?

- **Przed ważnymi zmianami** (dodawanie seansów, rezerwacji)
- **Codziennie** (jeśli używasz automatycznego backupu)
- **Przed aktualizacją aplikacji**

### Rozmiar plików backupu

- Pusta baza: ~10-20 KB
- Z danymi testowymi: ~50-200 KB
- Z pełnymi danymi produkcyjnymi: może być większy (zależy od ilości danych)

---

## Pomoc

Jeśli masz problemy:
1. Sprawdź czy Docker działa: `docker ps`
2. Sprawdź logi kontenera: `docker logs cinema-db`
3. Sprawdź czy baza działa: `docker exec cinema-db psql -U admin cinema_paradiso -c "\dt"`


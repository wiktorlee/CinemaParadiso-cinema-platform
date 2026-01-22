# CinemaParadiso - Plan Pracy i Architektura

## Spis Treści
1. [Stan Obecny](#stan-obecny)
2. [Architektura Aplikacji](#architektura-aplikacji)
3. [Warstwy Aplikacji (Analogia do Pythona)](#warstwy-aplikacji)
4. [Plan Implementacji](#plan-implementacji)
5. [Opcje do Wyboru](#opcje-do-wyboru)

---

## Stan Obecny

### Co już mamy:
- Docker Compose z PostgreSQL 16 - kontener działa
- Spring Boot 3.4.0 - framework aplikacji
- Maven - zarządzanie zależnościami
- Podstawowa struktura projektu - klasa główna aplikacji
- Konfiguracja bazy danych - połączenie z PostgreSQL
- Flyway - gotowy do migracji

### Co zostało zrobione:
1. docker-compose.yml - dodano volumes dla trwałości danych i healthcheck
2. application.properties - skonfigurowano połączenie z bazą danych
3. pom.xml - poprawiono wersję Spring Boot i zależności testowe

---

## Architektura Aplikacji

### Spring Boot - Co to jest?
Spring Boot to framework, który upraszcza tworzenie aplikacji Java. Automatycznie konfiguruje wiele rzeczy (auto-configuration), dzięki czemu możesz skupić się na logice biznesowej.

### Maven - Co to jest?
Maven to narzędzie do zarządzania projektami Java. Plik pom.xml (Project Object Model) definiuje:
- Zależności (biblioteki) - co potrzebujesz
- Wersje - jakich wersji używasz
- Pluginy - narzędzia do kompilacji, testów itp.

### PostgreSQL w Dockerze - Dlaczego?
- Izolacja - baza danych działa w osobnym kontenerze
- Łatwość - nie musisz instalować PostgreSQL na systemie
- Spójność - wszyscy mają takie samo środowisko
- Volumes - dane są trwałe (nie znikają po restarcie)

### Flyway - Co to jest?
Flyway to narzędzie do zarządzania migracjami bazy danych. Migracje to pliki SQL, które:
- Tworzą tabele
- Dodają kolumny
- Modyfikują strukturę bazy
- Są wersjonowane (V1__Create_table.sql, V2__Add_column.sql)

---

## Warstwy Aplikacji (Analogia do Pythona)

W Pythonie (np. Django/Flask) masz:
- Models (modele danych)
- Views (widoki/logika)
- Repositories/Services (warstwa dostępu do danych)

W Spring Boot masz podobną strukturę, ale z innymi nazwami:

### 1. Entity (Model)
Python: models.py z klasami Django Model
Java/Spring: @Entity - klasa reprezentująca tabelę w bazie

```java
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    // gettery, settery...
}
```

### 2. Repository (Dostęp do danych)
Python: Custom repository lub ORM queries
Java/Spring: Repository interface - Spring Data JPA automatycznie tworzy implementację

```java
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitle(String title);
}
```

### 3. Service (Logika biznesowa)
Python: Service layer z logiką biznesową
Java/Spring: @Service - klasa z logiką biznesową

```java
@Service
public class MovieService {
    private final MovieRepository movieRepository;
    
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
}
```

### 4. Controller (API Endpoints)
Python: Views/Route handlers
Java/Spring: @RestController lub @Controller - obsługuje HTTP requests

```java
@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;
    
    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }
}
```

### 5. DTO (Data Transfer Object)
Python: Serializers (Django REST Framework)
Java/Spring: Klasy do transferu danych między warstwami

```java
public class MovieDTO {
    private String title;
    private Integer duration;
    // tylko pola potrzebne do API
}
```

---

## Plan Implementacji

### Faza 1: Model Danych (Entity Layer)
Tworzenie encji reprezentujących dane w bazie:
- Movie (Film)
- Screening (Seans)
- Room (Sala)
- Seat (Miejsce)
- Reservation (Rezerwacja)
- User/Customer (Użytkownik/Klient)

Pytania do rozstrzygnięcia:
- Czy potrzebujemy systemu użytkowników? (logowanie, rejestracja)
- Czy rezerwacje są przypisane do użytkownika czy tylko email?
- Jakie dodatkowe informacje o filmach? (gatunek, reżyser, opis)

### Faza 2: Migracje Flyway
Tworzenie plików SQL w src/main/resources/db/migration/:
- V1__Create_movies_table.sql
- V2__Create_rooms_table.sql
- V3__Create_screenings_table.sql
- itd.

Pytania:
- Czy zaczynamy od migracji czy od Entity? (zalecam: Entity → migracje)

### Faza 3: Repository Layer
Tworzenie interfejsów Repository dla każdej encji:
- MovieRepository
- ScreeningRepository
- RoomRepository
- itd.

### Faza 4: Service Layer
Implementacja logiki biznesowej:
- MovieService
- ScreeningService
- ReservationService
- itd.

Pytania:
- Jakie operacje biznesowe? (rezerwacja miejsc, sprawdzanie dostępności)

### Faza 5: Controller Layer (REST API)
Tworzenie endpointów API:
- GET /api/movies - lista filmów
- GET /api/screenings - lista seansów
- POST /api/reservations - tworzenie rezerwacji
- itd.

### Faza 6: Frontend (HTML+CSS+JS)
Tworzenie interfejsu użytkownika:
- Strona główna z listą filmów
- Wybór seansu
- Wybór miejsc
- Formularz rezerwacji

### Faza 7: Walidacja i Obsługa Błędów
- Walidacja danych wejściowych (@Valid)
- Obsługa wyjątków (@ExceptionHandler)
- Komunikaty błędów dla użytkownika

---

## Opcje do Wyboru

### 1. System Użytkowników
Opcja A: Prosty (tylko email)
- Rezerwacje przypisane do emaila
- Brak logowania/rejestracji
- Szybsza implementacja

Opcja B: Pełny system użytkowników
- Tabela User z hasłami
- Spring Security (autoryzacja)
- Sesje/logowanie
- Więcej czasu, ale bardziej profesjonalne

Moja rekomendacja: Zacznij od Opcji A, później możesz rozszerzyć

---

### 2. Struktura Rezerwacji
Opcja A: Rezerwacja = 1 miejsce
- Każda rezerwacja to jedno miejsce
- Prostsze, ale więcej rekordów w bazie

Opcja B: Rezerwacja = wiele miejsc
- Jedna rezerwacja może zawierać wiele miejsc
- Bardziej realistyczne, ale bardziej złożone

Moja rekomendacja: Opcja B (bardziej profesjonalna)

---

### 3. Status Rezerwacji
Opcja A: Tylko aktywne rezerwacje
- Brak statusów (aktywna/anulowana)

Opcja B: Statusy rezerwacji
- PENDING (oczekująca)
- CONFIRMED (potwierdzona)
- CANCELLED (anulowana)
- EXPIRED (wygasła)

Moja rekomendacja: Opcja B (lepsza kontrola)

---

### 4. Ceny Biletów
Opcja A: Stała cena
- Wszystkie bilety kosztują tyle samo

Opcja B: Ceny zależne od typu
- Normalny, ulgowy, studencki
- Różne ceny dla różnych sal/seansów

Moja rekomendacja: Opcja A na start, później rozszerzyć

---

### 5. Zarządzanie Seansami
Opcja A: Ręczne dodawanie
- Admin dodaje seanse przez API/formularz
- Przykład: "Film 'Incepcja', Sala 1, 2024-01-15 18:00"
- Każdy seans dodawany osobno
- Prostsze do zrozumienia i implementacji

Opcja B: Automatyczne generowanie
- System generuje seanse na podstawie harmonogramu
- Przykład: "Film 'Incepcja' będzie grany w Salach 1-3, codziennie o 14:00, 17:00, 20:00 przez tydzień"
- System automatycznie tworzy wiele seansów na raz
- Bardziej zaawansowane, wymaga logiki harmonogramu

Wyjaśnienie:
- Seans (Screening) = konkretny pokaz filmu w konkretnej sali o konkretnej godzinie
- Przykład: "Incepcja, Sala 1, 15 stycznia 2024, 18:00"
- Zarządzanie seansami = jak te seanse są tworzone w systemie

Decyzja użytkownika: Do wyjaśnienia - zalecam Opcję A na start (prostsze, lepsze do nauki)

---

### 6. Frontend - Podejście
Opcja A: Vanilla JS
- Czysty HTML+CSS+JavaScript
- Fetch API do komunikacji z backendem
- Prostsze, ale więcej kodu

Opcja B: Prosty framework (np. Alpine.js)
- Lekki framework
- Mniej kodu, ale dodatkowa zależność

Moja rekomendacja: Opcja A (uczysz się podstaw)

---

## Następne Kroki

Proponuję następującą kolejność:

1. Zdefiniuj model danych - jakie encje potrzebujesz?
2. Stwórz pierwsze Entity - zacznij od Movie (Film)
3. Stwórz pierwszą migrację - V1__Create_movies_table.sql
4. Stwórz Repository - MovieRepository
5. Stwórz Service - MovieService
6. Stwórz Controller - MovieController (REST API)
7. Przetestuj - uruchom aplikację i sprawdź czy działa

---

## Słownik Terminów

- @Entity - adnotacja oznaczająca klasę jako encję bazy danych
- @Repository - adnotacja dla warstwy dostępu do danych
- @Service - adnotacja dla warstwy logiki biznesowej
- @RestController - adnotacja dla kontrolera REST API
- JPA - Java Persistence API (standard do pracy z bazami danych)
- Hibernate - implementacja JPA używana przez Spring
- DTO - Data Transfer Object (obiekt do transferu danych)
- Dependency Injection - Spring automatycznie wstrzykuje zależności
- @Autowired - adnotacja do automatycznego wstrzykiwania zależności

---

## Podjęte Decyzje

1. System użytkowników: Opcja B - Pełny system (Spring Security, logowanie, rejestracja)
2. Struktura rezerwacji: Opcja B - Wiele miejsc (jedna rezerwacja może zawierać wiele miejsc)
3. Statusy rezerwacji: Uproszczone (tylko ACTIVE i CANCELLED, bez PENDING/CONFIRMED)
4. Ceny biletów: Opcja B - Zaawansowane (różne w zależności od taryfy, seansu i miejsca - np. VIP)
5. Zarządzanie seansami: Opcja B - Automatyczne generowanie (harmonogramy, szablony seansów)
6. Frontend: Vanilla JS (HTML+CSS+JavaScript - nauka podstaw)

---

## Model Danych - Wymagania

Na podstawie decyzji, potrzebujemy następujących encji:

### 1. User (Użytkownik)
- id, email, hasło (hashowane), imię, nazwisko
- Role: USER, ADMIN
- Spring Security

### 2. Movie (Film)
- id, tytuł, opis, gatunek, reżyser, czas trwania, rok produkcji

### 3. Room (Sala)
- id, numer sali, liczba rzędów, liczba miejsc w rzędzie
- Może mieć różne układy miejsc (VIP, standardowe)

### 4. Seat (Miejsce)
- id, rząd, numer miejsca, typ (STANDARD, VIP)
- Powiązane z Room

### 5. Screening (Seans)
- id, film, sala, data i godzina rozpoczęcia, cena bazowa, cena VIP
- Powiązane z Movie i Room
- Może być wygenerowany automatycznie z harmonogramu

### 6. ScreeningSchedule (Harmonogram Seansów)
- id, film, sala, dzień tygodnia (np. PIĄTEK), godzina, data rozpoczęcia, data zakończenia
- Używany do automatycznego generowania seansów
- Przykład: "Film X, Sala 1, każdy piątek o 18:00, od 15.01 do 15.03"

### 7. Reservation (Rezerwacja)
- id, użytkownik, seans, data utworzenia, status (ACTIVE, CANCELLED)
- Jedna rezerwacja może zawierać wiele miejsc

### 8. ReservationSeat (Miejsce w Rezerwacji)
- id, rezerwacja, miejsce, typ biletu (NORMAL, REDUCED, STUDENT)
- Cena zależy od: typu biletu + typ miejsca (VIP/STANDARD) + seans

### 10. Pricing (Cennik) - opcjonalnie
- Można też przechowywać ceny w Screening lub jako osobna tabela

---

## Plan Implementacji - Kolejność

1. Struktura pakietów - organizacja kodu
2. User + Spring Security - system użytkowników
3. Movie - filmy
4. Room + Seat - sale i miejsca
5. Screening + ScreeningSchedule - seanse i harmonogramy (automatyczne generowanie)
6. Reservation + ReservationSeat - rezerwacje
7. Pricing logic - logika cenowa
8. Controllers (REST API) - endpointy
9. Frontend - interfejs użytkownika

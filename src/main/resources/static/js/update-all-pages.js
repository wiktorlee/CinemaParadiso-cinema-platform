/**
 * Skrypt pomocniczy do aktualizacji wszystkich stron z nowym headerem i footerem
 * Uruchom w konsoli przeglądarki na każdej stronie, aby zaktualizować header i footer
 */

function updateHeaderAndFooter() {
    // Header HTML
    const headerHTML = `
        <header class="p-3 text-bg-dark">
            <div class="container">
                <div class="header-content">
                    <a href="/index.html" class="logo-section">
                        <div class="logo-icon">
                            <i class="bi bi-film"></i>
                        </div>
                        <span class="logo-text">Cinema Paradiso${window.location.pathname.includes('/admin/') ? ' - Panel Admina' : ''}</span>
                    </a>
                    
                    <ul class="nav-links">
                        <li><a href="/index.html" class="nav-link">Strona Główna</a></li>
                        <li><a href="/movies.html" class="nav-link">Filmy</a></li>
                        <li><a href="/repertuar.html" class="nav-link">Repertuar</a></li>
                    </ul>
                    
                    <div class="header-actions">
                        <a href="/login.html" id="loginLink" class="btn-header btn-outline-light-custom">Zaloguj się</a>
                        <a href="/register.html" id="registerLink" class="btn-header btn-warning-custom">Zarejestruj się</a>
                        <a href="/profile.html" id="profileLink" class="btn-header btn-outline-light-custom" style="display: none;">Mój Profil</a>
                        <a href="/admin/movies.html" id="adminMoviesLink" class="btn-header btn-outline-light-custom" style="display: none;">Zarządzanie Filmami</a>
                        <a href="/admin/rooms.html" id="adminRoomsLink" class="btn-header btn-outline-light-custom" style="display: none;">Zarządzanie Salami</a>
                        <a href="/admin/screenings.html" id="adminScreeningsLink" class="btn-header btn-outline-light-custom" style="display: none;">Zarządzanie Seansami</a>
                        <a href="/admin/audit-logs.html" id="adminAuditLogsLink" class="btn-header btn-outline-light-custom" style="display: none;">Logi Audytowe</a>
                        <button id="logoutBtn" class="btn-header" style="display: none;">Wyloguj</button>
                    </div>
                </div>
            </div>
        </header>
    `;
    
    // Footer HTML
    const footerHTML = `
        <footer>
            <div class="footer-content">
                <div class="footer-grid">
                    <div class="footer-section">
                        <h5>Cinema Paradiso</h5>
                        <p style="color: rgba(255, 255, 255, 0.7); margin: 0;">
                            System zarządzania rezerwacjami biletów do kina. 
                            Najlepsze filmy w najlepszych cenach.
                        </p>
                    </div>
                    
                    <div class="footer-section">
                        <h5>Nawigacja</h5>
                        <ul>
                            <li><a href="/index.html">Strona Główna</a></li>
                            <li><a href="/movies.html">Filmy</a></li>
                            <li><a href="/repertuar.html">Repertuar</a></li>
                            <li><a href="/login.html">Zaloguj się</a></li>
                        </ul>
                    </div>
                    
                    <div class="footer-section">
                        <h5>Informacje</h5>
                        <ul>
                            <li><a href="/register.html">Rejestracja</a></li>
                            <li><a href="/profile.html">Mój Profil</a></li>
                            <li><a href="/reservation.html">Rezerwacje</a></li>
                        </ul>
                    </div>
                    
                    <div class="footer-section">
                        <h5>Kontakt</h5>
                        <p style="color: rgba(255, 255, 255, 0.7); margin: 0 0 0.5rem 0;">
                            Email: kontakt@cinemaparadiso.pl<br>
                            Telefon: +48 123 456 789
                        </p>
                    </div>
                </div>
                
                <div class="footer-bottom">
                    <div>
                        <p style="margin: 0; color: rgba(255, 255, 255, 0.6);">
                            © 2025 Cinema Paradiso. Wszelkie prawa zastrzeżone.
                        </p>
                    </div>
                    <div class="footer-social">
                        <a href="#" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
                        <a href="#" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
                        <a href="#" aria-label="Twitter"><i class="bi bi-twitter"></i></a>
                    </div>
                </div>
            </div>
        </footer>
    `;
    
    // Zastąp header
    const existingHeader = document.querySelector('header');
    if (existingHeader) {
        existingHeader.outerHTML = headerHTML;
    }
    
    // Dodaj footer jeśli nie istnieje
    const existingFooter = document.querySelector('footer');
    if (!existingFooter) {
        document.body.insertAdjacentHTML('beforeend', footerHTML);
    }
}

// Uruchom jeśli jesteśmy w konsoli
if (typeof window !== 'undefined') {
    console.log('Uruchom updateHeaderAndFooter() aby zaktualizować header i footer');
}


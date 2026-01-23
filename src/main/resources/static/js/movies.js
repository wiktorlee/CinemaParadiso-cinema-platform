/**
 * Skrypt dla strony z listą filmów
 */

let currentPage = 0;
let currentSearch = '';
let currentGenre = '';
let currentPageSize = 12; // Zmniejszone z 20 na 12 dla szybszego ładowania

document.addEventListener('DOMContentLoaded', async () => {
    const moviesContainer = document.getElementById('moviesContainer');
    const profileLink = document.getElementById('profileLink');
    const adminLink = document.getElementById('adminLink');
    const loginLink = document.getElementById('loginLink');
    const logoutBtn = document.getElementById('logoutBtn');
    const searchForm = document.getElementById('searchForm');
    const searchInput = document.getElementById('searchInput');
    const genreFilter = document.getElementById('genreFilter');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    
    // Sprawdź czy użytkownik jest zalogowany
    try {
        const user = await getCurrentUser();
        
        // Użytkownik jest zalogowany
        profileLink.style.display = 'inline-block';
        logoutBtn.style.display = 'inline-block';
        loginLink.style.display = 'none';
        
        // Jeśli admin, pokaż link do panelu admina
        if (user.role === 'ADMIN') {
            adminLink.style.display = 'inline-block';
        }
        
    } catch (error) {
        // Użytkownik nie jest zalogowany
        profileLink.style.display = 'none';
        adminLink.style.display = 'none';
        logoutBtn.style.display = 'none';
        loginLink.style.display = 'inline-block';
    }
    
    // Obsługa wylogowania
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            try {
                await logout();
                window.location.reload();
            } catch (error) {
                console.error('Logout error:', error);
                window.location.reload();
            }
        });
    }
    
    // Obsługa formularza wyszukiwania
    searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        currentSearch = searchInput.value.trim();
        currentGenre = '';
        genreFilter.value = '';
        currentPage = 0;
        loadMovies();
    });
    
    // Obsługa filtru gatunku
    genreFilter.addEventListener('change', () => {
        currentGenre = genreFilter.value;
        currentSearch = '';
        searchInput.value = '';
        currentPage = 0;
        loadMovies();
    });
    
    // Obsługa przycisku wyczyść
    clearSearchBtn.addEventListener('click', () => {
        currentSearch = '';
        currentGenre = '';
        searchInput.value = '';
        genreFilter.value = '';
        currentPage = 0;
        clearSearchBtn.style.display = 'none';
        loadMovies();
    });
    
    // Załaduj filmy
    await loadMovies();
});

/**
 * Ładuje i wyświetla listę filmów (z paginacją i wyszukiwaniem)
 */
async function loadMovies() {
    const container = document.getElementById('moviesContainer');
    const resultsInfo = document.getElementById('resultsInfo');
    const paginationContainer = document.getElementById('paginationContainer');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    
    // Pokaż loader podczas ładowania
    container.innerHTML = '<div class="text-center p-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Ładowanie...</span></div><p class="mt-2 text-muted">Ładowanie filmów...</p></div>';
    
    try {
        // Buduj URL z parametrami
        let url = '/api/movies?page=' + currentPage + '&size=' + currentPageSize + '&sort=title,asc';
        
        if (currentSearch) {
            url += '&search=' + encodeURIComponent(currentSearch);
        }
        if (currentGenre) {
            url += '&genre=' + encodeURIComponent(currentGenre);
        }
        
        const response = await fetch(url, {
            method: 'GET',
            credentials: 'include'
        });
        
        if (!response.ok) {
            throw new Error('HTTP error! status: ' + response.status);
        }
        
        const data = await response.json();
        
        // Sprawdź czy to odpowiedź z paginacją czy zwykła lista
        let movies, totalElements, totalPages, hasNext, hasPrevious;
        
        if (Array.isArray(data)) {
            // Stara odpowiedź (bez paginacji) - dla kompatybilności
            movies = data;
            totalElements = movies.length;
            totalPages = 1;
            hasNext = false;
            hasPrevious = false;
        } else {
            // Nowa odpowiedź (z paginacją)
            movies = data.content || [];
            totalElements = data.totalElements || 0;
            totalPages = data.totalPages || 1;
            hasNext = data.hasNext || false;
            hasPrevious = data.hasPrevious || false;
        }
        
        // Pokaż/ukryj przycisk wyczyść
        if (currentSearch || currentGenre) {
            clearSearchBtn.style.display = 'inline-block';
        } else {
            clearSearchBtn.style.display = 'none';
        }
        
        // Wyświetl informację o wynikach
        if (currentSearch || currentGenre) {
            let infoText = `Znaleziono ${totalElements} film${totalElements === 1 ? '' : totalElements < 5 ? 'y' : 'ów'}`;
            if (currentSearch) {
                infoText += ` dla "${currentSearch}"`;
            }
            if (currentGenre) {
                infoText += ` w gatunku "${currentGenre}"`;
            }
            resultsInfo.textContent = infoText;
            resultsInfo.style.display = 'block';
        } else {
            resultsInfo.style.display = 'none';
        }
        
        if (movies.length === 0) {
            container.innerHTML = '<p>Brak filmów spełniających kryteria wyszukiwania.</p>';
            paginationContainer.style.display = 'none';
            return;
        }
        
        // Zapisz filmy globalnie
        allMovies = movies;
        
        // Wyświetl filmy z lazy loading dla obrazów
        container.innerHTML = movies.map(movie => `
            <div class="movie-card" data-movie-id="${movie.id}" onclick="showMovieDetails(${movie.id})">
                ${movie.posterPath ? 
                    `<img src="${movie.posterPath}" alt="${movie.title}" class="movie-poster" loading="lazy" decoding="async" onerror="this.onerror=null; this.parentElement.innerHTML='<div class=\\'movie-poster-placeholder\\'>${movie.title}</div>';" onload="this.style.opacity='1';" style="opacity: 0; transition: opacity 0.3s;">` :
                    `<div class="movie-poster-placeholder">${movie.title}</div>`
                }
                <div class="movie-info">
                    <h3>${movie.title}</h3>
                </div>
            </div>
        `).join('');
        
        // Wyświetl paginację (jeśli jest więcej niż jedna strona)
        if (totalPages > 1) {
            displayPagination(totalPages, hasNext, hasPrevious);
            paginationContainer.style.display = 'flex';
        } else {
            paginationContainer.style.display = 'none';
        }
        
    } catch (error) {
        console.error('Error loading movies:', error);
        container.innerHTML = '<p class="error-message">Wystąpił błąd podczas ładowania filmów.</p>';
        paginationContainer.style.display = 'none';
    }
}

/**
 * Wyświetla paginację
 */
function displayPagination(totalPages, hasNext, hasPrevious) {
    const paginationContainer = document.getElementById('paginationContainer');
    
    let paginationHTML = '<div class="pagination">';
    
    // Przycisk poprzednia strona
    if (hasPrevious) {
        paginationHTML += `<button class="pagination-btn" onclick="goToPage(${currentPage - 1})">‹ Poprzednia</button>`;
    } else {
        paginationHTML += `<button class="pagination-btn" disabled>‹ Poprzednia</button>`;
    }
    
    // Numer strony
    paginationHTML += `<span class="pagination-info">Strona ${currentPage + 1} z ${totalPages}</span>`;
    
    // Przycisk następna strona
    if (hasNext) {
        paginationHTML += `<button class="pagination-btn" onclick="goToPage(${currentPage + 1})">Następna ›</button>`;
    } else {
        paginationHTML += `<button class="pagination-btn" disabled>Następna ›</button>`;
    }
    
    paginationHTML += '</div>';
    paginationContainer.innerHTML = paginationHTML;
}

/**
 * Przechodzi do określonej strony
 */
window.goToPage = function(page) {
    currentPage = page;
    loadMovies();
    // Przewiń do góry strony
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Przechowuj wszystkie filmy globalnie dla łatwego dostępu
let allMovies = [];

/**
 * Pokazuje szczegóły filmu w modalu
 * Funkcja globalna dostępna z onclick
 */
window.showMovieDetails = async function(movieId) {
    try {
        // Jeśli nie mamy jeszcze wszystkich filmów, pobierz szczegóły
        let movie = allMovies.find(m => m.id === movieId);
        if (!movie) {
            movie = await getMovieById(movieId);
        }
        
        // Utwórz modal
        const modal = document.createElement('div');
        modal.className = 'movie-modal';
        modal.innerHTML = `
            <div class="movie-modal-content">
                <span class="movie-modal-close">&times;</span>
                <div class="movie-modal-body">
                    <div class="movie-modal-poster">
                        ${movie.posterPath ? 
                            `<img src="${movie.posterPath}" alt="${movie.title}" loading="eager" decoding="async" onerror="this.style.display='none';">` :
                            `<div class="movie-poster-placeholder-large">${movie.title}</div>`
                        }
                    </div>
                    <div class="movie-modal-info">
                        <h2>${movie.title}</h2>
                        ${movie.director ? `<p><strong>Reżyser:</strong> ${movie.director}</p>` : ''}
                        ${movie.genre ? `<p><strong>Gatunek:</strong> ${movie.genre}</p>` : ''}
                        ${movie.durationMinutes ? `<p><strong>Czas trwania:</strong> ${movie.durationMinutes} minut</p>` : ''}
                        ${movie.year ? `<p><strong>Rok produkcji:</strong> ${movie.year}</p>` : ''}
                        ${movie.releaseDate ? `<p><strong>Data premiery:</strong> ${new Date(movie.releaseDate).toLocaleDateString('pl-PL')}</p>` : ''}
                        ${movie.description ? `<div class="movie-modal-description"><strong>Opis:</strong><p>${movie.description}</p></div>` : ''}
                        <div class="movie-modal-screenings" id="screenings-${movieId}">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                                <p><strong>Seanse:</strong></p>
                                <button class="btn btn-sm btn-secondary" onclick="refreshScreenings(${movieId})" id="refreshBtn-${movieId}">
                                    <i class="bi bi-arrow-clockwise"></i> Odśwież
                                </button>
                            </div>
                            <p class="info-message">Ładowanie seansów...</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
        
        // Załaduj seanse dla tego filmu
        loadScreeningsForMovie(movieId);
        
        // Obsługa zamknięcia modala
        const closeBtn = modal.querySelector('.movie-modal-close');
        closeBtn.onclick = () => {
            document.body.removeChild(modal);
        };
        
        modal.onclick = (e) => {
            if (e.target === modal) {
                document.body.removeChild(modal);
            }
        };
        
        // Zamknij na ESC
        const handleEsc = (e) => {
            if (e.key === 'Escape') {
                document.body.removeChild(modal);
                document.removeEventListener('keydown', handleEsc);
            }
        };
        document.addEventListener('keydown', handleEsc);
        
    } catch (error) {
        console.error('Error loading movie details:', error);
        alert('Wystąpił błąd podczas ładowania szczegółów filmu.');
    }
}

/**
 * Ładuje seanse dla danego filmu
 */
async function loadScreeningsForMovie(movieId) {
    const container = document.getElementById(`screenings-${movieId}`);
    
    try {
        const response = await getScreeningsByMovie(movieId, 0, 100);
        const screenings = response.content || [];
        
        // Filtruj tylko przyszłe seanse
        const now = new Date();
        const upcomingScreenings = screenings.filter(screening => {
            const screeningDate = new Date(screening.startTime);
            return screeningDate > now;
        }).sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
        
        if (upcomingScreenings.length === 0) {
            container.innerHTML = '<p class="info-message">Brak dostępnych seansów dla tego filmu.</p>';
            return;
        }
        
        container.innerHTML = `
            <div class="screenings-list">
                ${upcomingScreenings.map(screening => {
                    const date = new Date(screening.startTime);
                    const dateStr = date.toLocaleDateString('pl-PL', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit'
                    });
                    const timeStr = date.toLocaleTimeString('pl-PL', {
                        hour: '2-digit',
                        minute: '2-digit'
                    });
                    
                    return `
                        <div class="screening-item" style="padding: 10px; margin: 5px 0; background: var(--light); border-radius: 5px; display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong>${dateStr} ${timeStr}</strong> - Sala ${screening.roomNumber}
                                <br>
                                <small>Cena: ${screening.basePrice.toFixed(2)} PLN</small>
                                ${screening.availableSeats !== undefined ? `<br><small>Dostępne miejsca: ${screening.availableSeats}/${screening.totalSeats}</small>` : ''}
                            </div>
                            <a href="/reservation.html?screeningId=${screening.id}" class="btn btn-primary btn-sm">
                                <i class="bi bi-ticket-perforated"></i> Rezerwuj
                            </a>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    } catch (error) {
        console.error('Error loading screenings:', error);
        container.innerHTML = '<p class="error-message">Nie udało się załadować seansów.</p>';
    }
}

/**
 * Odświeża listę seansów dla filmu
 */
window.refreshScreenings = function(movieId) {
    const container = document.getElementById(`screenings-${movieId}`);
    const refreshBtn = document.getElementById(`refreshBtn-${movieId}`);
    
    if (refreshBtn) {
        refreshBtn.disabled = true;
        refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Odświeżanie...';
    }
    
    container.innerHTML = '<p class="info-message">Ładowanie seansów...</p>';
    
    loadScreeningsForMovie(movieId).finally(() => {
        if (refreshBtn) {
            refreshBtn.disabled = false;
            refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Odśwież';
        }
    });
}


/**
 * Skrypt dla strony z listą filmów
 */

let currentPage = 0;
let currentSearch = '';
let currentGenre = '';
let currentPageSize = 20;

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
        
        // Wyświetl filmy
        container.innerHTML = movies.map(movie => `
            <div class="movie-card" data-movie-id="${movie.id}" onclick="showMovieDetails(${movie.id})">
                ${movie.posterPath ? 
                    `<img src="${movie.posterPath}" alt="${movie.title}" class="movie-poster" onerror="this.onerror=null; this.parentElement.innerHTML='<div class=\\'movie-poster-placeholder\\'>${movie.title}</div>';">` :
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
                            `<img src="${movie.posterPath}" alt="${movie.title}" onerror="this.style.display='none';">` :
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
                    </div>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
        
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


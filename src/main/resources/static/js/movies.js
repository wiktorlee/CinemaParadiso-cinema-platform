/**
 * Skrypt dla strony z listą filmów
 */

let currentPage = 0;
let currentSearch = '';
let currentGenre = '';
let currentSort = 'title,asc';
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
    const sortSelect = document.getElementById('sortSelect');
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
    
    // Obsługa sortowania
    if (sortSelect) {
        sortSelect.addEventListener('change', () => {
            currentSort = sortSelect.value;
            currentPage = 0;
            loadMovies();
        });
    }
    
    // Obsługa przycisku wyczyść
    clearSearchBtn.addEventListener('click', () => {
        currentSearch = '';
        currentGenre = '';
        searchInput.value = '';
        genreFilter.value = '';
        if (sortSelect) {
            sortSelect.value = 'title,asc';
            currentSort = 'title,asc';
        }
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
        let url = '/api/movies?page=' + currentPage + '&size=' + currentPageSize + '&sort=' + encodeURIComponent(currentSort);
        
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
                    ${movie.averageRating !== null && movie.averageRating !== undefined ? `
                        <div style="display: flex; align-items: center; gap: 5px; margin-top: 5px;">
                            ${generateStars(movie.averageRating)}
                            <small style="color: #666;">${movie.averageRating.toFixed(1)} (${movie.totalRatings || 0})</small>
                        </div>
                    ` : ''}
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
        
        // Sprawdź czy użytkownik jest zalogowany
        let isLoggedIn = false;
        try {
            await getCurrentUser();
            isLoggedIn = true;
        } catch (e) {
            isLoggedIn = false;
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
                        
                        <!-- Sekcja ocen -->
                        ${movie.averageRating !== null && movie.averageRating !== undefined ? `
                            <div class="movie-rating-section" style="margin: 20px 0; padding: 15px; background: #f8f9fa; border-radius: 8px;">
                                <div style="display: flex; align-items: center; gap: 15px; margin-bottom: 15px;">
                                    <div>
                                        <strong>Ocena:</strong>
                                        <div style="display: flex; align-items: center; gap: 5px; margin-top: 5px;">
                                            ${generateStars(movie.averageRating)}
                                            <span style="font-size: 1.2em; font-weight: bold; margin-left: 10px;">
                                                ${movie.averageRating.toFixed(1)}
                                            </span>
                                            <span style="color: #666; margin-left: 5px;">
                                                (${movie.totalRatings || 0} ${movie.totalRatings === 1 ? 'ocena' : movie.totalRatings < 5 ? 'oceny' : 'ocen'})
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- Sekcja oceniania (tylko dla zalogowanych) -->
                                <div id="rating-section-${movieId}" style="margin-top: 15px;">
                                    ${isLoggedIn ? (
                                        movie.userRating ? `
                                            <p>Twoja ocena: ${generateStars(movie.userRating)}</p>
                                            <button class="btn btn-sm btn-outline-primary" onclick="showRatingModal(${movieId}, ${movie.userRating})">
                                                Zmień ocenę
                                            </button>
                                        ` : `
                                            <button class="btn btn-sm btn-primary" onclick="showRatingModal(${movieId})">
                                                <i class="bi bi-star"></i> Oceń ten film
                                            </button>
                                        `
                                    ) : `
                                        <small class="text-muted">Zaloguj się, aby ocenić ten film</small>
                                    `}
                                </div>
                            </div>
                        ` : `
                            <div class="movie-rating-section" style="margin: 20px 0; padding: 15px; background: #f8f9fa; border-radius: 8px;">
                                <p>Ten film nie ma jeszcze ocen.</p>
                                <div id="rating-section-${movieId}">
                                    ${isLoggedIn ? `
                                        <button class="btn btn-sm btn-primary" onclick="showRatingModal(${movieId})">
                                            <i class="bi bi-star"></i> Oceń ten film
                                        </button>
                                    ` : `
                                        <small class="text-muted">Zaloguj się, aby ocenić ten film</small>
                                    `}
                                </div>
                            </div>
                        `}
                        
                        <!-- Sekcja recenzji -->
                        <div class="movie-reviews-section" id="reviews-section-${movieId}" style="margin: 20px 0;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                                <h3>Recenzje</h3>
                                ${isLoggedIn ? `
                                    <button class="btn btn-sm btn-primary" onclick="showReviewForm(${movieId})" id="add-review-btn-${movieId}">
                                        <i class="bi bi-pencil"></i> Napisz recenzję
                                    </button>
                                ` : `
                                    <small class="text-muted">Zaloguj się, aby napisać recenzję</small>
                                `}
                            </div>
                            <div id="reviews-list-${movieId}">
                                <p class="info-message">Ładowanie recenzji...</p>
                            </div>
                        </div>
                        
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
        
        // Załaduj recenzje
        loadReviews(movieId);
        
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

/**
 * Generuje HTML z gwiazdkami dla oceny
 */
function generateStars(rating) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    let starsHTML = '';
    
    for (let i = 0; i < fullStars; i++) {
        starsHTML += '<i class="bi bi-star-fill" style="color: #ffc107;"></i>';
    }
    if (hasHalfStar) {
        starsHTML += '<i class="bi bi-star-half" style="color: #ffc107;"></i>';
    }
    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '<i class="bi bi-star" style="color: #ddd;"></i>';
    }
    
    return starsHTML;
}

/**
 * Pokazuje modal do oceniania filmu
 */
window.showRatingModal = async function(movieId, currentRating = null) {
    // Sprawdź czy użytkownik jest zalogowany
    try {
        await getCurrentUser();
    } catch (error) {
        alert('Musisz być zalogowany, aby ocenić film.');
        return;
    }
    
    const modal = document.createElement('div');
    modal.className = 'rating-modal';
    modal.style.cssText = 'position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 10000; display: flex; align-items: center; justify-content: center;';
    
    modal.innerHTML = `
        <div style="background: white; padding: 30px; border-radius: 10px; max-width: 400px; width: 90%;">
            <h3>Oceń ten film</h3>
            <div id="stars-container" style="display: flex; gap: 10px; justify-content: center; margin: 20px 0; font-size: 2em;">
                ${[1,2,3,4,5].map(i => `
                    <i class="bi bi-star${currentRating && i <= currentRating ? '-fill' : ''}" 
                       data-rating="${i}" 
                       style="cursor: pointer; color: ${currentRating && i <= currentRating ? '#ffc107' : '#ddd'};"
                       onmouseover="highlightStars(${i})"
                       onmouseout="resetStars(${currentRating || 0})"
                       onclick="submitRating(${movieId}, ${i})"></i>
                `).join('')}
            </div>
            <p style="text-align: center; color: #666;">Wybierz liczbę gwiazdek (1-5)</p>
            <button class="btn btn-secondary" onclick="this.closest('.rating-modal').remove()" style="width: 100%; margin-top: 10px;">Anuluj</button>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Zamknij po kliknięciu poza modalem
    modal.onclick = (e) => {
        if (e.target === modal) {
            modal.remove();
        }
    };
};

/**
 * Podświetla gwiazdki przy najechaniu
 */
window.highlightStars = function(rating) {
    const stars = document.querySelectorAll('#stars-container i');
    stars.forEach((star, index) => {
        const starRating = index + 1;
        if (starRating <= rating) {
            star.className = 'bi bi-star-fill';
            star.style.color = '#ffc107';
        } else {
            star.className = 'bi bi-star';
            star.style.color = '#ddd';
        }
    });
};

/**
 * Resetuje gwiazdki do poprzedniej wartości
 */
window.resetStars = function(currentRating) {
    const stars = document.querySelectorAll('#stars-container i');
    stars.forEach((star, index) => {
        const starRating = index + 1;
        if (starRating <= currentRating) {
            star.className = 'bi bi-star-fill';
            star.style.color = '#ffc107';
        } else {
            star.className = 'bi bi-star';
            star.style.color = '#ddd';
        }
    });
};

/**
 * Wysyła ocenę do API
 */
window.submitRating = async function(movieId, rating) {
    try {
        const response = await apiRequest(`/movies/${movieId}/rate`, {
            method: 'POST',
            body: JSON.stringify({ rating: rating })
        });
        
        // Zamknij modal
        document.querySelector('.rating-modal').remove();
        
        // Odśwież szczegóły filmu
        const modal = document.querySelector('.movie-modal');
        if (modal) {
            modal.remove();
            showMovieDetails(movieId);
        }
        
        // Pokaż komunikat sukcesu
        alert('Dziękujemy za ocenę!');
        
    } catch (error) {
        console.error('Error submitting rating:', error);
        if (error.message && error.message.includes('UNAUTHORIZED')) {
            alert('Musisz być zalogowany, aby ocenić film.');
        } else {
            alert('Wystąpił błąd podczas zapisywania oceny.');
        }
    }
};

/**
 * Pokazuje formularz do pisania recenzji
 */
window.showReviewForm = async function(movieId) {
    // Sprawdź czy użytkownik jest zalogowany
    try {
        await getCurrentUser();
    } catch (error) {
        alert('Musisz być zalogowany, aby napisać recenzję.');
        return;
    }
    
    const modal = document.createElement('div');
    modal.className = 'review-modal';
    modal.style.cssText = 'position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 10000; display: flex; align-items: center; justify-content: center;';
    
    modal.innerHTML = `
        <div style="background: white; padding: 30px; border-radius: 10px; max-width: 600px; width: 90%; max-height: 80vh; overflow-y: auto;">
            <h3>Napisz recenzję</h3>
            <form id="review-form" onsubmit="submitReview(event, ${movieId})">
                <div class="mb-3">
                    <label for="review-content" class="form-label">Treść recenzji (min. 10 znaków)</label>
                    <textarea id="review-content" class="form-control" rows="6" required minlength="10" maxlength="5000"></textarea>
                    <small class="form-text text-muted">Maksymalnie 5000 znaków</small>
                </div>
                <div style="display: flex; gap: 10px;">
                    <button type="submit" class="btn btn-primary">Opublikuj recenzję</button>
                    <button type="button" class="btn btn-secondary" onclick="this.closest('.review-modal').remove()">Anuluj</button>
                </div>
            </form>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Zamknij po kliknięciu poza modalem
    modal.onclick = (e) => {
        if (e.target === modal) {
            modal.remove();
        }
    };
};

/**
 * Wysyła recenzję do API
 */
window.submitReview = async function(event, movieId) {
    event.preventDefault();
    
    const content = document.getElementById('review-content').value.trim();
    
    if (content.length < 10) {
        alert('Recenzja musi mieć co najmniej 10 znaków.');
        return;
    }
    
    try {
        await apiRequest(`/movies/${movieId}/reviews`, {
            method: 'POST',
            body: JSON.stringify({ content: content })
        });
        
        // Zamknij modal
        document.querySelector('.review-modal').remove();
        
        // Odśwież listę recenzji
        loadReviews(movieId);
        
        alert('Recenzja została opublikowana!');
        
    } catch (error) {
        console.error('Error submitting review:', error);
        if (error.message && error.message.includes('UNAUTHORIZED')) {
            alert('Musisz być zalogowany, aby napisać recenzję.');
        } else {
            alert('Wystąpił błąd podczas publikowania recenzji.');
        }
    }
};

/**
 * Ładuje recenzje dla filmu
 */
async function loadReviews(movieId, page = 0) {
    const container = document.getElementById(`reviews-list-${movieId}`);
    if (!container) return;
    
    try {
        const response = await apiRequest(`/movies/${movieId}/reviews?page=${page}&size=10`, {
            method: 'GET'
        });
        
        const reviews = response.content || [];
        const totalElements = response.totalElements || 0;
        
        if (reviews.length === 0) {
            container.innerHTML = '<p class="info-message">Ten film nie ma jeszcze recenzji. Bądź pierwszy!</p>';
            return;
        }
        
        // Sprawdź czy użytkownik jest zalogowany
        let currentUser = null;
        try {
            currentUser = await getCurrentUser();
        } catch (e) {
            // Użytkownik nie jest zalogowany
        }
        
        // Sprawdź czy przycisk "Napisz recenzję" powinien być widoczny
        const addReviewBtn = document.getElementById(`add-review-btn-${movieId}`);
        if (addReviewBtn && !currentUser) {
            addReviewBtn.style.display = 'none';
        }
        
        container.innerHTML = `
            <div class="reviews-list">
                ${reviews.map(review => `
                    <div class="review-item" style="padding: 15px; margin: 10px 0; background: white; border-radius: 8px; border-left: 4px solid #007bff;">
                        <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 10px;">
                            <div>
                                <strong>${review.firstName} ${review.lastName}</strong>
                                <small class="text-muted" style="display: block;">
                                    ${new Date(review.createdAt).toLocaleDateString('pl-PL', {
                                        year: 'numeric',
                                        month: 'long',
                                        day: 'numeric',
                                        hour: '2-digit',
                                        minute: '2-digit'
                                    })}
                                    ${review.updatedAt !== review.createdAt ? '(edytowana)' : ''}
                                </small>
                            </div>
                            ${review.isOwnReview ? `
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteReview(${review.id}, ${movieId})">
                                    <i class="bi bi-trash"></i> Usuń
                                </button>
                            ` : ''}
                        </div>
                        <p style="white-space: pre-wrap; margin: 0;">${escapeHtml(review.content)}</p>
                    </div>
                `).join('')}
            </div>
            ${totalElements > 10 && reviews.length < totalElements ? `
                <div style="text-align: center; margin-top: 15px;">
                    <button class="btn btn-sm btn-outline-primary" onclick="loadMoreReviews(${movieId}, ${page + 1})">
                        Załaduj więcej recenzji
                    </button>
                </div>
            ` : ''}
        `;
        
    } catch (error) {
        console.error('Error loading reviews:', error);
        container.innerHTML = '<p class="error-message">Nie udało się załadować recenzji.</p>';
    }
}

/**
 * Usuwa recenzję
 */
window.deleteReview = async function(reviewId, movieId) {
    if (!confirm('Czy na pewno chcesz usunąć tę recenzję?')) {
        return;
    }
    
    try {
        await apiRequest(`/reviews/${reviewId}`, {
            method: 'DELETE'
        });
        
        // Odśwież listę recenzji
        loadReviews(movieId);
        alert('Recenzja została usunięta.');
        
    } catch (error) {
        console.error('Error deleting review:', error);
        alert('Wystąpił błąd podczas usuwania recenzji.');
    }
};

/**
 * Ładuje więcej recenzji
 */
window.loadMoreReviews = function(movieId, page) {
    loadReviews(movieId, page);
};

/**
 * Escapuje HTML w tekście (zabezpieczenie przed XSS)
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}


/**
 * Główny skrypt aplikacji
 * Używany na stronie głównej (index.html)
 * Nawigacja jest obsługiwana przez navigation.js
 */

document.addEventListener('DOMContentLoaded', async () => {
    const userInfoDiv = document.getElementById('userInfo');
    const userDetailsP = document.getElementById('userDetails');
    const notLoggedInDiv = document.getElementById('notLoggedIn');
    
    // Załaduj filmy do carousel
    await loadCarouselMovies();
    
    // Sprawdź czy użytkownik jest zalogowany (tylko dla wyświetlenia informacji na stronie głównej)
    try {
        const user = await getCurrentUser();
        
        // Użytkownik jest zalogowany - pokaż informacje
        if (userInfoDiv && userDetailsP && notLoggedInDiv) {
            userInfoDiv.style.display = 'block';
            notLoggedInDiv.style.display = 'none';
            
            // Wyświetl informacje o użytkowniku
            userDetailsP.innerHTML = `
                <strong>Username:</strong> ${user.username}<br>
                <strong>Imię:</strong> ${user.firstName}<br>
                <strong>Nazwisko:</strong> ${user.lastName}<br>
                <strong>Rola:</strong> ${user.role}
            `;
        }
        
    } catch (error) {
        // Użytkownik nie jest zalogowany
        if (userInfoDiv && notLoggedInDiv) {
            userInfoDiv.style.display = 'none';
            notLoggedInDiv.style.display = 'block';
        }
    }
    
    // Nawigacja jest obsługiwana przez navigation.js
});

/**
 * Ładuje filmy do carousel na stronie głównej
 * Miesza najpopularniejsze filmy i najnowsze premiery
 */
async function loadCarouselMovies() {
    try {
        // Pobierz najpopularniejsze filmy i najnowsze premiery
        const [popularMovies, latestMovies] = await Promise.all([
            fetch('/api/movies/popular?limit=3').then(res => res.json()),
            fetch('/api/movies/latest?limit=3').then(res => res.json())
        ]);
        
        // Połącz i wymieszaj filmy (maksymalnie 5 filmów)
        const allMovies = [...popularMovies, ...latestMovies];
        // Usuń duplikaty (jeśli film jest zarówno popularny jak i najnowszy)
        const uniqueMovies = allMovies.filter((movie, index, self) =>
            index === self.findIndex(m => m.id === movie.id)
        ).slice(0, 5);
        
        // Jeśli nie ma filmów, nie rób nic
        if (uniqueMovies.length === 0) {
            console.warn('Brak filmów do wyświetlenia w carousel');
            return;
        }
        
        // Zaktualizuj carousel
        updateCarousel(uniqueMovies);
        
    } catch (error) {
        console.error('Błąd podczas ładowania filmów do carousel:', error);
    }
}

/**
 * Aktualizuje carousel z filmami
 */
function updateCarousel(movies) {
    const carouselInner = document.querySelector('#mainCarousel .carousel-inner');
    const carouselIndicators = document.querySelector('#mainCarousel .carousel-indicators');
    
    if (!carouselInner || !carouselIndicators) {
        console.error('Nie znaleziono elementów carousel');
        return;
    }
    
    // Wyczyść istniejące slajdy
    carouselInner.innerHTML = '';
    carouselIndicators.innerHTML = '';
    
    // Utwórz nowe slajdy
    movies.forEach((movie, index) => {
        const isActive = index === 0;
        
        // Indicator
        const indicator = document.createElement('button');
        indicator.type = 'button';
        indicator.setAttribute('data-bs-target', '#mainCarousel');
        indicator.setAttribute('data-bs-slide-to', index.toString());
        indicator.setAttribute('aria-label', `Slide ${index + 1}`);
        if (isActive) {
            indicator.classList.add('active');
            indicator.setAttribute('aria-current', 'true');
        }
        carouselIndicators.appendChild(indicator);
        
        // Slajd
        const slide = document.createElement('div');
        slide.className = `carousel-item ${isActive ? 'active' : ''}`;
        
        // Tło slajdu (okładka filmu lub gradient)
        const backgroundStyle = movie.posterPath 
            ? `background-image: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.7)), url('${movie.posterPath}'); background-size: cover; background-position: center;`
            : 'background: linear-gradient(135deg, var(--primary) 0%, var(--dark) 100%);';
        
        slide.innerHTML = `
            <div class="carousel-slide-content" style="${backgroundStyle}">
                <div class="container">
                    <div class="carousel-caption ${index % 2 === 0 ? 'text-start' : index % 2 === 1 ? 'text-center' : 'text-end'}">
                        <h1>${escapeHtml(movie.title)}</h1>
                        <p>${escapeHtml(movie.description || '')}</p>
                        ${movie.averageRating !== null && movie.averageRating !== undefined ? `
                            <div style="margin: 15px 0;">
                                <span style="font-size: 1.2em; color: var(--accent);">
                                    ${generateStarsHTML(movie.averageRating)}
                                </span>
                                <span style="margin-left: 10px; font-size: 1.1em;">
                                    ${movie.averageRating.toFixed(1)} (${movie.totalRatings || 0} ${movie.totalRatings === 1 ? 'ocena' : movie.totalRatings < 5 ? 'oceny' : 'ocen'})
                                </span>
                            </div>
                        ` : ''}
                        <p>
                            <a class="btn btn-lg btn-primary" href="/movies.html" onclick="event.stopPropagation();">
                                Zobacz szczegóły
                            </a>
                        </p>
                    </div>
                </div>
            </div>
        `;
        
        carouselInner.appendChild(slide);
    });
    
    // Reinicjalizuj carousel Bootstrap (jeśli potrzeba)
    const carouselElement = document.getElementById('mainCarousel');
    if (carouselElement && typeof bootstrap !== 'undefined') {
        // Bootstrap 5 automatycznie inicjalizuje carousel z data-bs-ride="carousel"
        // Ale możemy wymusić aktualizację
        const carousel = bootstrap.Carousel.getInstance(carouselElement);
        if (carousel) {
            carousel.dispose();
        }
        new bootstrap.Carousel(carouselElement, {
            interval: 5000, // Zmiana slajdu co 5 sekund
            ride: 'carousel'
        });
    }
}

/**
 * Generuje HTML dla gwiazdek oceny
 */
function generateStarsHTML(rating) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    
    let starsHTML = '';
    for (let i = 0; i < fullStars; i++) {
        starsHTML += '<i class="bi bi-star-fill"></i>';
    }
    if (hasHalfStar) {
        starsHTML += '<i class="bi bi-star-half"></i>';
    }
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '<i class="bi bi-star"></i>';
    }
    return starsHTML;
}

/**
 * Escapuje HTML aby zapobiec XSS
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}



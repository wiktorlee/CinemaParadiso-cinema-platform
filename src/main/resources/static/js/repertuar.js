/**
 * Skrypt dla strony repertuaru
 */

document.addEventListener('DOMContentLoaded', async () => {
    const dateInput = document.getElementById('dateInput');
    const todayBtn = document.getElementById('todayBtn');
    const tomorrowBtn = document.getElementById('tomorrowBtn');
    const repertoireContainer = document.getElementById('repertoireContainer');
    const noScreeningsMessage = document.getElementById('noScreeningsMessage');
    
    // Ustaw domyślną datę na dzisiaj
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];
    dateInput.value = todayStr;
    
    // Załaduj repertuar dla dzisiejszej daty
    await loadRepertoire(todayStr);
    
    // Obsługa zmiany daty
    dateInput.addEventListener('change', async (e) => {
        await loadRepertoire(e.target.value);
    });
    
    // Przycisk "Dzisiaj"
    todayBtn.addEventListener('click', () => {
        dateInput.value = todayStr;
        loadRepertoire(todayStr);
    });
    
    // Przycisk "Jutro"
    tomorrowBtn.addEventListener('click', () => {
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        const tomorrowStr = tomorrow.toISOString().split('T')[0];
        dateInput.value = tomorrowStr;
        loadRepertoire(tomorrowStr);
    });
    
    /**
     * Ładuje repertuar dla wybranej daty
     */
    async function loadRepertoire(date) {
        try {
            repertoireContainer.innerHTML = '<p class="text-muted">Ładowanie repertuaru...</p>';
            noScreeningsMessage.style.display = 'none';
            
            const response = await apiRequest(`/screenings/repertoire?date=${date}`, {
                method: 'GET'
            });
            
            if (!response || response.length === 0) {
                repertoireContainer.innerHTML = '';
                noScreeningsMessage.style.display = 'block';
                return;
            }
            
            displayRepertoire(response);
            
        } catch (error) {
            console.error('Błąd podczas ładowania repertuaru:', error);
            repertoireContainer.innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle"></i> 
                    Wystąpił błąd podczas ładowania repertuaru. Spróbuj ponownie.
                </div>
            `;
        }
    }
    
    /**
     * Wyświetla repertuar na stronie
     */
    function displayRepertoire(repertoire) {
        if (!repertoire || repertoire.length === 0) {
            repertoireContainer.innerHTML = '';
            noScreeningsMessage.style.display = 'block';
            return;
        }
        
        noScreeningsMessage.style.display = 'none';
        
        let html = '<div class="repertoire-grid">';
        
        repertoire.forEach(movie => {
            // Okładka filmu (jeśli dostępna)
            const posterHtml = movie.moviePosterPath 
                ? `<img src="${movie.moviePosterPath}" alt="${movie.movieTitle}" class="movie-poster" onerror="this.style.display='none'">`
                : `<div class="movie-poster-placeholder"><i class="bi bi-film"></i></div>`;
            
            // Lista godzin seansów
            let screeningsHtml = '';
            if (movie.screenings && movie.screenings.length > 0) {
                screeningsHtml = '<div class="screenings-list">';
                movie.screenings.forEach(screening => {
                    const timeStr = formatTime(screening.startTime);
                    const availableSeats = screening.availableSeats || 0;
                    const totalSeats = screening.totalSeats || 0;
                    const seatsPercentage = totalSeats > 0 ? Math.round((availableSeats / totalSeats) * 100) : 0;
                    
                    // Kolor w zależności od dostępności miejsc
                    let seatClass = 'seats-available';
                    if (seatsPercentage < 20) {
                        seatClass = 'seats-low';
                    } else if (seatsPercentage < 50) {
                        seatClass = 'seats-medium';
                    }
                    
                    screeningsHtml += `
                        <div class="screening-time-item">
                            <a href="/reservation.html?screeningId=${screening.screeningId}" class="screening-link">
                                <div class="screening-time">
                                    <span class="time">${timeStr}</span>
                                    <span class="room">Sala ${screening.roomNumber}</span>
                                </div>
                                <div class="screening-info">
                                    <span class="price">${formatPrice(screening.basePrice)}</span>
                                    <span class="seats ${seatClass}">
                                        <i class="bi bi-people"></i> ${availableSeats}/${totalSeats}
                                    </span>
                                </div>
                            </a>
                        </div>
                    `;
                });
                screeningsHtml += '</div>';
            } else {
                screeningsHtml = '<p class="text-muted">Brak seansów</p>';
            }
            
            html += `
                <div class="repertoire-movie-card">
                    <div class="movie-poster-container">
                        ${posterHtml}
                    </div>
                    <div class="movie-info">
                        <h3 class="movie-title">${escapeHtml(movie.movieTitle)}</h3>
                        <div class="movie-meta">
                            <span class="movie-genre"><i class="bi bi-tag"></i> ${escapeHtml(movie.movieGenre || 'Brak gatunku')}</span>
                            <span class="movie-duration"><i class="bi bi-clock"></i> ${movie.movieDurationMinutes || 0} min</span>
                        </div>
                        ${screeningsHtml}
                    </div>
                </div>
            `;
        });
        
        html += '</div>';
        repertoireContainer.innerHTML = html;
    }
    
    /**
     * Formatuje czas (HH:mm)
     */
    function formatTime(timeStr) {
        if (!timeStr) return '';
        // Jeśli to string w formacie "HH:mm:ss" lub "HH:mm"
        if (typeof timeStr === 'string') {
            return timeStr.substring(0, 5); // Pobierz tylko HH:mm
        }
        return timeStr;
    }
    
    /**
     * Formatuje cenę
     */
    function formatPrice(price) {
        if (!price) return 'Brak ceny';
        return parseFloat(price).toFixed(2) + ' zł';
    }
    
    /**
     * Escapuje HTML (zabezpieczenie przed XSS)
     */
    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});


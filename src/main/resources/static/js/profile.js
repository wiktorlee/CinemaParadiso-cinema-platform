/**
 * Skrypt dla strony profilu użytkownika
 */

document.addEventListener('DOMContentLoaded', async () => {
    // Nawigacja jest obsługiwana przez navigation.js
    
    // Sprawdź czy użytkownik jest zalogowany
    try {
        const user = await getCurrentUser();
        
        // Wyświetl informacje o użytkowniku
        document.getElementById('username').textContent = user.username;
        document.getElementById('firstName').textContent = user.firstName;
        document.getElementById('lastName').textContent = user.lastName;
        document.getElementById('role').textContent = user.role;
        
        // Załaduj rezerwacje (na razie placeholder)
        loadReservations();
        
        // Załaduj recenzje (na razie placeholder)
        loadReviews();
        
        // Obsługa formularza zmiany hasła
        setupChangePasswordForm();
        
    } catch (error) {
        // Użytkownik nie jest zalogowany - przekieruj do logowania
        console.error('User not logged in:', error);
        window.location.href = '/login.html';
    }
    
    // Obsługa wylogowania jest w navigation.js
});

/**
 * Ładuje rezerwacje użytkownika
 */
async function loadReservations() {
    const container = document.getElementById('reservationsContainer');
    
    try {
        const reservations = await apiRequest('/reservations/my', { method: 'GET' });
        
        if (reservations.length === 0) {
            container.innerHTML = `
                <p class="info-message">
                    Nie masz jeszcze żadnych rezerwacji. 
                    <a href="/movies.html">Przejdź do filmów</a>, aby zarezerwować miejsca.
                </p>
            `;
            return;
        }
        
        container.innerHTML = reservations.map(reservation => {
            const statusBadge = reservation.status === 'ACTIVE' 
                ? '<span class="badge bg-success">Aktywna</span>'
                : '<span class="badge bg-secondary">Anulowana</span>';
            
            const seatsList = reservation.seats.map(seat => 
                `Rząd ${seat.rowNumber}, Miejsce ${seat.seatNumber} (${getTicketTypeName(seat.ticketType)})`
            ).join(', ');
            
            const cancelButton = reservation.status === 'ACTIVE' 
                ? `<button class="btn btn-sm btn-danger" onclick="cancelReservation(${reservation.id})">
                    <i class="bi bi-x-circle"></i> Anuluj
                   </button>`
                : '';
            
            return `
                <div class="reservation-card" style="background: white; padding: 20px; margin: 10px 0; border-radius: 8px; border: 1px solid var(--secondary);">
                    <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 15px;">
                        <div>
                            <h4>${reservation.movieTitle}</h4>
                            <p><strong>Sala:</strong> ${reservation.roomNumber}</p>
                            <p><strong>Data i godzina:</strong> ${formatDateTime(reservation.screeningStartTime)}</p>
                            <p><strong>Data rezerwacji:</strong> ${formatDateTime(reservation.createdAt)}</p>
                        </div>
                        <div>
                            ${statusBadge}
                        </div>
                    </div>
                    <div style="margin-bottom: 15px;">
                        <p><strong>Miejsca:</strong> ${seatsList}</p>
                        <p><strong>Całkowita cena:</strong> <strong style="color: var(--primary); font-size: 1.2em;">${reservation.totalPrice.toFixed(2)} PLN</strong></p>
                    </div>
                    <div style="margin-bottom: 15px;">
                        <button class="btn btn-sm btn-info" onclick="showRoomOccupancy(${reservation.screeningId})" id="occupancyBtn-${reservation.screeningId}">
                            <i class="bi bi-grid-3x3-gap"></i> Pokaż wypełnienie sali
                        </button>
                        <div id="occupancy-${reservation.screeningId}" style="display: none; margin-top: 15px;"></div>
                    </div>
                    <div>
                        ${cancelButton}
                    </div>
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error loading reservations:', error);
        container.innerHTML = `
            <p class="error-message">
                Wystąpił błąd podczas ładowania rezerwacji: ${error.message || 'Nieznany błąd'}
            </p>
        `;
    }
}

/**
 * Anuluje rezerwację
 */
async function cancelReservation(reservationId) {
    if (!confirm('Czy na pewno chcesz anulować tę rezerwację?')) {
        return;
    }
    
    try {
        await apiRequest(`/reservations/${reservationId}`, { method: 'DELETE' });
        alert('Rezerwacja została anulowana');
        loadReservations(); // Odśwież listę
    } catch (error) {
        console.error('Error canceling reservation:', error);
        alert('Wystąpił błąd podczas anulowania rezerwacji: ' + (error.message || 'Nieznany błąd'));
    }
}

/**
 * Zwraca nazwę typu biletu
 */
function getTicketTypeName(ticketType) {
    const names = {
        'NORMAL': 'Normalny',
        'REDUCED': 'Ulgowy',
        'STUDENT': 'Studencki'
    };
    return names[ticketType] || ticketType;
}

/**
 * Formatuje datę i godzinę
 */
function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Pokazuje wypełnienie sali dla seansu
 */
async function showRoomOccupancy(screeningId) {
    const container = document.getElementById(`occupancy-${screeningId}`);
    const button = document.getElementById(`occupancyBtn-${screeningId}`);
    
    if (container.style.display === 'none') {
        // Pokaż wypełnienie
        container.style.display = 'block';
        container.innerHTML = '<p class="info-message">Ładowanie danych sali...</p>';
        button.innerHTML = '<i class="bi bi-grid-3x3-gap"></i> Ukryj wypełnienie sali';
        
        try {
            // Pobierz dostępne miejsca
            const seats = await apiRequest(`/reservations/screenings/${screeningId}/seats`, { method: 'GET' });
            
            if (!seats || seats.length === 0) {
                container.innerHTML = '<p class="error-message">Brak danych o miejscach dla tego seansu.</p>';
                return;
            }
            
            // Pobierz szczegóły seansu dla informacji o sali
            const screening = await apiRequest(`/screenings/${screeningId}`, { method: 'GET' });
            
            // Grupuj miejsca po rzędach
            const seatsByRow = new Map();
            seats.forEach(seat => {
                if (!seatsByRow.has(seat.rowNumber)) {
                    seatsByRow.set(seat.rowNumber, []);
                }
                seatsByRow.get(seat.rowNumber).push(seat);
            });
            
            // Sortuj rzędy
            const sortedRows = Array.from(seatsByRow.keys()).sort((a, b) => a - b);
            
            // Oblicz statystyki
            const totalSeats = seats.length;
            const availableSeats = seats.filter(s => s.isAvailable && s.isSeatEnabled).length;
            const reservedSeats = seats.filter(s => !s.isAvailable).length;
            const disabledSeats = seats.filter(s => !s.isSeatEnabled).length;
            const vipSeats = seats.filter(s => s.seatType === 'VIP' || s.seatType === 'Vip').length;
            
            console.log('Seats data:', seats.slice(0, 5)); // Debug - pierwsze 5 miejsc
            
            // Renderuj siatkę
            let gridHTML = `
                <div style="background: var(--light); padding: 15px; border-radius: 8px; margin-top: 10px;">
                    <h5>Wypełnienie sali</h5>
                    <div style="display: flex; gap: 20px; margin-bottom: 15px; flex-wrap: wrap;">
                        <div><strong>Wszystkie miejsca:</strong> ${totalSeats}</div>
                        <div><strong>Dostępne:</strong> <span style="color: green;">${availableSeats}</span></div>
                        <div><strong>Zarezerwowane:</strong> <span style="color: #999;">${reservedSeats}</span></div>
                        <div><strong>Niedostępne:</strong> <span style="color: red;">${disabledSeats}</span></div>
                        <div><strong>VIP:</strong> <span style="color: var(--accent);">${vipSeats}</span></div>
                        <div><strong>Wypełnienie:</strong> ${Math.round((reservedSeats / totalSeats) * 100)}%</div>
                    </div>
                    <div style="text-align: center; margin-bottom: 10px; padding: 10px; background: linear-gradient(to bottom, #666, #333); color: white; border-radius: 5px; font-weight: bold;">
                        <i class="bi bi-display"></i> EKRAN
                    </div>
                    <div class="seat-grid-mini" style="display: flex; flex-direction: column; gap: 3px; align-items: center;">
            `;
            
            sortedRows.forEach(rowNumber => {
                const seatsInRow = seatsByRow.get(rowNumber).sort((a, b) => a.seatNumber - b.seatNumber);
                
                gridHTML += `<div style="display: flex; gap: 2px; align-items: center;">`;
                gridHTML += `<span style="width: 25px; text-align: center; font-size: 11px; font-weight: bold;">${rowNumber}</span>`;
                
                seatsInRow.forEach(seat => {
                    let backgroundColor = '';
                    let borderColor = '';
                    let title = `Rząd ${seat.rowNumber}, Miejsce ${seat.seatNumber}`;
                    
                    // Sprawdź status miejsca w odpowiedniej kolejności
                    if (!seat.isSeatEnabled) {
                        backgroundColor = '#f44336';
                        borderColor = '#d32f2f';
                        title += ' (Niedostępne)';
                    } else if (!seat.isAvailable) {
                        backgroundColor = '#ccc';
                        borderColor = '#999';
                        title += ' (Zarezerwowane)';
                    } else if (seat.seatType === 'VIP') {
                        backgroundColor = '#e5ba41'; // var(--accent)
                        borderColor = '#2d3c59'; // var(--primary)
                        title += ' (VIP - Dostępne)';
                    } else {
                        backgroundColor = '#e8f5e9';
                        borderColor = '#2d3c59'; // var(--primary)
                        title += ' (Dostępne)';
                    }
                    
                    gridHTML += `<div class="seat-mini" title="${title}" style="width: 20px; height: 20px; background-color: ${backgroundColor}; border: 1px solid ${borderColor}; border-radius: 3px; font-size: 8px; display: flex; align-items: center; justify-content: center; cursor: help;"></div>`;
                });
                
                gridHTML += `</div>`;
            });
            
            gridHTML += `
                    </div>
                    <div style="display: flex; gap: 15px; justify-content: center; margin-top: 15px; flex-wrap: wrap; font-size: 12px;">
                        <div style="display: flex; align-items: center; gap: 5px;">
                            <div style="width: 15px; height: 15px; background: #e8f5e9; border: 1px solid var(--primary); border-radius: 3px;"></div>
                            <span>Dostępne</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 5px;">
                            <div style="width: 15px; height: 15px; background: var(--accent); border: 1px solid var(--primary); border-radius: 3px;"></div>
                            <span>VIP</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 5px;">
                            <div style="width: 15px; height: 15px; background: #ccc; border: 1px solid #999; border-radius: 3px;"></div>
                            <span>Zajęte</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 5px;">
                            <div style="width: 15px; height: 15px; background: #f44336; border: 1px solid #d32f2f; border-radius: 3px;"></div>
                            <span>Niedostępne</span>
                        </div>
                    </div>
                </div>
            `;
            
            container.innerHTML = gridHTML;
            
        } catch (error) {
            console.error('Error loading room occupancy:', error);
            container.innerHTML = '<p class="error-message">Nie udało się załadować danych sali.</p>';
        }
    } else {
        // Ukryj wypełnienie
        container.style.display = 'none';
        button.innerHTML = '<i class="bi bi-grid-3x3-gap"></i> Pokaż wypełnienie sali';
    }
}

/**
 * Konfiguruje formularz zmiany hasła
 */
function setupChangePasswordForm() {
    const form = document.getElementById('changePasswordForm');
    const messageDiv = document.getElementById('changePasswordMessage');
    
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        
        // Walidacja po stronie klienta
        if (newPassword.length < 6) {
            messageDiv.innerHTML = '<p class="error-message">Nowe hasło musi mieć co najmniej 6 znaków</p>';
            return;
        }
        
        if (newPassword !== confirmPassword) {
            messageDiv.innerHTML = '<p class="error-message">Nowe hasło i potwierdzenie hasła nie pasują do siebie</p>';
            return;
        }
        
        try {
            messageDiv.innerHTML = '<p class="info-message">Zmienianie hasła...</p>';
            
            await apiRequest('/profile/change-password', {
                method: 'POST',
                body: JSON.stringify({
                    currentPassword: currentPassword,
                    newPassword: newPassword,
                    confirmPassword: confirmPassword
                })
            });
            
            messageDiv.innerHTML = '<p class="success-message">Hasło zostało zmienione pomyślnie!</p>';
            form.reset();
            
            // Ukryj komunikat po 5 sekundach
            setTimeout(() => {
                messageDiv.innerHTML = '';
            }, 5000);
            
        } catch (error) {
            console.error('Error changing password:', error);
            messageDiv.innerHTML = `<p class="error-message">${error.message || 'Wystąpił błąd podczas zmiany hasła'}</p>`;
        }
    });
}

/**
 * Ładuje recenzje użytkownika
 * Na razie wyświetla placeholder - będzie zaimplementowane później
 */
async function loadReviews() {
    const container = document.getElementById('reviewsContainer');
    
    try {
        // TODO: Stworzyć endpoint /api/reviews/my
        // const reviews = await apiRequest('/reviews/my', { method: 'GET' });
        
        // Placeholder - na razie nie ma jeszcze endpointu
        container.innerHTML = `
            <p class="info-message">
                Funkcja recenzji będzie dostępna wkrótce. 
                Tutaj zobaczysz wszystkie swoje recenzje filmów.
            </p>
        `;
    } catch (error) {
        console.error('Error loading reviews:', error);
        container.innerHTML = `
            <p class="error-message">
                Wystąpił błąd podczas ładowania recenzji.
            </p>
        `;
    }
}


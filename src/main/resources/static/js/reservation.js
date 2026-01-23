/**
 * Skrypt dla strony rezerwacji miejsc
 */

let screeningData = null;
let seatsData = [];
let selectedSeats = new Map(); // Map<seatId, {seat, ticketType}>

document.addEventListener('DOMContentLoaded', async () => {
    // Sprawdź czy użytkownik jest zalogowany
    try {
        const user = await getCurrentUser();
    } catch (error) {
        // Użytkownik nie jest zalogowany - przekieruj do logowania
        console.error('User not logged in:', error);
        modalService.alert(
            'Wymagane logowanie',
            'Musisz być zalogowany, aby zarezerwować miejsca.',
            null,
            { type: 'info' }
        );
        window.location.href = '/login.html';
        return;
    }
    
    // Pobierz ID seansu z URL
    const urlParams = new URLSearchParams(window.location.search);
    const screeningId = urlParams.get('screeningId');
    
    if (!screeningId) {
        showError('Brak ID seansu w URL');
        return;
    }
    
    // Załaduj dane seansu i miejsc
    await loadScreeningData(screeningId);
    await loadSeatsData(screeningId);
});

/**
 * Ładuje dane seansu
 */
async function loadScreeningData(screeningId) {
    try {
        // Pokaż loader podczas ładowania danych seansu
        loaderService.showFullScreen('Ładowanie danych seansu...');
        
        screeningData = await apiRequest(`/screenings/${screeningId}`, { method: 'GET' });
        
        const screeningInfo = document.getElementById('screeningInfo');
        screeningInfo.innerHTML = `
            <h3>${screeningData.movieTitle}</h3>
            <p><strong>Sala:</strong> ${screeningData.roomNumber}</p>
            <p><strong>Data i godzina:</strong> ${formatDateTime(screeningData.startTime)}</p>
            <p><strong>Cena bazowa:</strong> ${screeningData.basePrice.toFixed(2)} PLN</p>
            ${screeningData.vipPrice ? `<p><strong>Cena VIP:</strong> ${screeningData.vipPrice.toFixed(2)} PLN</p>` : ''}
        `;
        
        loaderService.hideFullScreen();
        document.getElementById('reservationContent').style.display = 'block';
    } catch (error) {
        console.error('Error loading screening data:', error);
        loaderService.hideFullScreen();
        showError('Nie udało się załadować danych seansu');
    }
}

/**
 * Ładuje dane miejsc
 */
async function loadSeatsData(screeningId) {
    try {
        seatsData = await apiRequest(`/reservations/screenings/${screeningId}/seats`, { method: 'GET' });
        renderSeatGrid();
    } catch (error) {
        console.error('Error loading seats data:', error);
        showError('Nie udało się załadować danych miejsc');
    }
}

/**
 * Renderuje siatkę miejsc
 */
function renderSeatGrid() {
    const seatGrid = document.getElementById('seatGrid');
    seatGrid.innerHTML = '';
    
    // Grupuj miejsca po rzędach
    const seatsByRow = new Map();
    seatsData.forEach(seat => {
        if (!seatsByRow.has(seat.rowNumber)) {
            seatsByRow.set(seat.rowNumber, []);
        }
        seatsByRow.get(seat.rowNumber).push(seat);
    });
    
    // Sortuj rzędy
    const sortedRows = Array.from(seatsByRow.keys()).sort((a, b) => a - b);
    
    // Renderuj każdy rząd
    sortedRows.forEach(rowNumber => {
        const rowDiv = document.createElement('div');
        rowDiv.className = 'seat-row';
        
        // Etykieta rzędu
        const rowLabel = document.createElement('div');
        rowLabel.className = 'row-label';
        rowLabel.textContent = rowNumber;
        rowDiv.appendChild(rowLabel);
        
        // Miejsca w rzędzie (posortowane po numerze miejsca)
        const seatsInRow = seatsByRow.get(rowNumber).sort((a, b) => a.seatNumber - b.seatNumber);
        
        seatsInRow.forEach(seat => {
            const seatDiv = document.createElement('div');
            seatDiv.className = 'seat';
            seatDiv.dataset.seatId = seat.seatId;
            
            // Dodaj klasy CSS w zależności od stanu miejsca
            if (!seat.isSeatEnabled) {
                seatDiv.classList.add('disabled');
            } else if (!seat.isAvailable) {
                seatDiv.classList.add('reserved');
            } else {
                seatDiv.classList.add('available');
                if (seat.seatType === 'VIP') {
                    seatDiv.classList.add('vip');
                }
                seatDiv.addEventListener('click', () => toggleSeat(seat));
            }
            
            seatDiv.textContent = seat.seatNumber;
            rowDiv.appendChild(seatDiv);
        });
        
        seatGrid.appendChild(rowDiv);
    });
}

/**
 * Przełącza wybór miejsca
 */
function toggleSeat(seat) {
    if (selectedSeats.has(seat.seatId)) {
        // Usuń z wybranych
        selectedSeats.delete(seat.seatId);
        updateSeatVisualState(seat.seatId, false);
    } else {
        // Dodaj do wybranych
        selectedSeats.set(seat.seatId, {
            seat: seat,
            ticketType: 'NORMAL' // Domyślnie normalny bilet
        });
        updateSeatVisualState(seat.seatId, true);
    }
    
    updateSelectedSeatsList();
}

/**
 * Aktualizuje wizualny stan miejsca
 */
function updateSeatVisualState(seatId, isSelected) {
    const seatElement = document.querySelector(`.seat[data-seat-id="${seatId}"]`);
    if (!seatElement) return;
    
    if (isSelected) {
        seatElement.classList.add('selected');
    } else {
        seatElement.classList.remove('selected');
    }
}

/**
 * Aktualizuje listę wybranych miejsc
 */
function updateSelectedSeatsList() {
    const selectedSeatsList = document.getElementById('selectedSeatsList');
    const selectedSeatsContainer = document.getElementById('selectedSeats');
    
    if (selectedSeats.size === 0) {
        selectedSeatsContainer.style.display = 'none';
        return;
    }
    
    selectedSeatsContainer.style.display = 'block';
    selectedSeatsList.innerHTML = '';
    
    let totalPrice = 0;
    
    selectedSeats.forEach((data, seatId) => {
        const seat = data.seat;
        const ticketType = data.ticketType;
        
        // Oblicz cenę (uproszczone - w rzeczywistości powinno być z backendu)
        const basePrice = seat.seatType === 'VIP' && screeningData.vipPrice 
            ? screeningData.vipPrice 
            : screeningData.basePrice;
        
        let price = basePrice;
        if (ticketType === 'REDUCED') {
            price = basePrice * 0.8;
        } else if (ticketType === 'STUDENT') {
            price = basePrice * 0.7;
        }
        
        totalPrice += price;
        
        const itemDiv = document.createElement('div');
        itemDiv.className = 'selected-seat-item';
        itemDiv.innerHTML = `
            <div>
                <strong>Rząd ${seat.rowNumber}, Miejsce ${seat.seatNumber}</strong>
                ${seat.seatType === 'VIP' ? '<span class="badge bg-warning">VIP</span>' : ''}
            </div>
            <div style="display: flex; gap: 10px; align-items: center;">
                <select class="ticket-type-select" data-seat-id="${seatId}">
                    <option value="NORMAL" ${ticketType === 'NORMAL' ? 'selected' : ''}>Normalny</option>
                    <option value="REDUCED" ${ticketType === 'REDUCED' ? 'selected' : ''}>Ulgowy</option>
                    <option value="STUDENT" ${ticketType === 'STUDENT' ? 'selected' : ''}>Studencki</option>
                </select>
                <span><strong>${price.toFixed(2)} PLN</strong></span>
                <button class="remove-seat-btn" onclick="removeSeat(${seatId})">
                    <i class="bi bi-x"></i>
                </button>
            </div>
        `;
        
        // Obsługa zmiany typu biletu
        const select = itemDiv.querySelector('.ticket-type-select');
        select.addEventListener('change', (e) => {
            data.ticketType = e.target.value;
            updateSelectedSeatsList();
        });
        
        selectedSeatsList.appendChild(itemDiv);
    });
    
    document.getElementById('totalPrice').textContent = `Razem: ${totalPrice.toFixed(2)} PLN`;
}

/**
 * Usuwa miejsce z wybranych
 */
function removeSeat(seatId) {
    selectedSeats.delete(seatId);
    updateSeatVisualState(seatId, false);
    updateSelectedSeatsList();
}

/**
 * Potwierdza rezerwację
 */
document.getElementById('confirmReservationBtn').addEventListener('click', async () => {
    if (selectedSeats.size === 0) {
        notificationService.showWarning('Wybierz przynajmniej jedno miejsce');
        return;
    }
    
    if (!screeningData) {
        notificationService.showError('Brak danych seansu');
        return;
    }
    
    // Przygotuj dane do wysłania
    const seats = Array.from(selectedSeats.entries()).map(([seatId, data]) => ({
        seatId: parseInt(seatId),
        ticketType: data.ticketType
    }));
    
    const reservationData = {
        screeningId: screeningData.id,
        seats: seats
    };
    
    try {
        // Pokaż loader w przycisku
        loaderService.showButton('confirmReservationBtn', 'Przetwarzanie rezerwacji...');
        
        const result = await apiRequest('/reservations', {
            method: 'POST',
            body: JSON.stringify(reservationData)
        });
        
        // Przekieruj do strony płatności
        window.location.href = `/payment.html?reservationId=${result.id}`;
    } catch (error) {
        console.error('Error creating reservation:', error);
        notificationService.showError('Wystąpił błąd podczas tworzenia rezerwacji: ' + (error.message || 'Nieznany błąd'));
        
        loaderService.hideButton('confirmReservationBtn');
    }
});

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
 * Wyświetla błąd
 */
function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    document.getElementById('loadingMessage').style.display = 'none';
}



/**
 * Skrypt dla strony płatności - wersja z kafelkami
 */

let reservationData = null;
let selectedPaymentMethod = null;

document.addEventListener('DOMContentLoaded', async () => {
    console.log('Payment page: DOMContentLoaded');
    
    try {
        // Sprawdź czy użytkownik jest zalogowany (z timeoutem)
        let user = null;
        try {
            const userPromise = getCurrentUser();
            const timeoutPromise = new Promise((_, reject) => 
                setTimeout(() => reject(new Error('Timeout')), 5000)
            );
            user = await Promise.race([userPromise, timeoutPromise]);
            
            if (!user) {
                throw new Error('Użytkownik nie jest zalogowany');
            }
            console.log('User logged in:', user.username);
        } catch (error) {
            console.error('User not logged in:', error);
            if (error.message !== 'Timeout') {
                alert('Musisz być zalogowany, aby dokonać płatności.');
                window.location.href = '/login.html';
                return;
            } else {
                console.warn('Timeout podczas sprawdzania użytkownika - kontynuuję');
            }
        }
        
        // Pobierz ID rezerwacji z URL
        const urlParams = new URLSearchParams(window.location.search);
        const reservationId = urlParams.get('reservationId');
        
        if (!reservationId) {
            console.error('Brak reservationId w URL');
            showError('Brak ID rezerwacji w URL');
            return;
        }
        
        console.log('Loading reservation data for ID:', reservationId);
        
        // Załaduj dane rezerwacji
        await loadReservationData(reservationId);
        
        console.log('Reservation data loaded, initializing payment method selection');
        
        // Inicjalizuj wybór metody płatności
        initializePaymentMethodSelection();
        
        console.log('Payment method selection initialized');
    } catch (error) {
        console.error('Błąd podczas inicjalizacji strony płatności:', error);
        showError('Wystąpił błąd podczas ładowania strony. Spróbuj odświeżyć stronę.');
    }
});

/**
 * Ładuje dane rezerwacji
 */
async function loadReservationData(reservationId) {
    try {
        reservationData = await apiRequest(`/reservations/${reservationId}`, { method: 'GET' });
        
        if (!reservationData) {
            throw new Error('Brak danych rezerwacji w odpowiedzi');
        }
        
        // Ustaw ID rezerwacji w formularzu
        const reservationIdInput = document.getElementById('reservationId');
        if (reservationIdInput) {
            reservationIdInput.value = reservationId;
        }
    } catch (error) {
        console.error('Error loading reservation data:', error);
        showError('Nie udało się załadować danych rezerwacji: ' + (error.message || 'Nieznany błąd'));
    }
}

/**
 * Inicjalizuje wybór metody płatności
 */
function initializePaymentMethodSelection() {
    // Obsługa kliknięć w kafelki
    const pricingCards = document.querySelectorAll('.pricing-card');
    pricingCards.forEach(card => {
        card.addEventListener('click', (event) => {
            // Jeśli kliknięto w przycisk, użyj jego data-method
            const button = event.target.closest('button[data-method]');
            const method = button ? button.getAttribute('data-method') : card.getAttribute('data-method');
            
            if (method) {
                selectPaymentMethod(method);
            }
        });
    });
    
    // Obsługa przycisku powrotu
    const backButton = document.getElementById('backButton');
    if (backButton) {
        backButton.addEventListener('click', () => {
            showPaymentMethodSelection();
        });
    }
}

/**
 * Wybiera metodę płatności i pokazuje panel podsumowania
 */
function selectPaymentMethod(method) {
    console.log('Selected payment method:', method);
    selectedPaymentMethod = method;
    
    // Ukryj wybór metody
    const methodSelection = document.getElementById('paymentMethodSelection');
    const summaryPanel = document.getElementById('paymentSummaryPanel');
    
    if (methodSelection) methodSelection.style.display = 'none';
    if (summaryPanel) {
        summaryPanel.classList.add('active');
        summaryPanel.style.display = 'block';
    }
    
    // Ustaw metodę w formularzu
    const methodInput = document.getElementById('selectedPaymentMethod');
    if (methodInput) methodInput.value = method;
    
    // Dla gotówki - od razu potwierdź bez płatności
    if (method === 'CASH') {
        handleCashPayment();
        return;
    }
    
    // Dla karty i BLIK - pokaż formularz
    showPaymentForm(method);
    
    // Wyświetl podsumowanie
    displaySummary();
}

/**
 * Obsługuje płatność gotówką - automatycznie oznacza rezerwację jako opłaconą
 */
async function handleCashPayment() {
    // Ukryj formularz płatności
    const paymentForm = document.getElementById('paymentForm');
    if (paymentForm) paymentForm.style.display = 'none';
    
    // Pokaż loader
    const summaryContent = document.getElementById('summaryContent');
    if (summaryContent) {
        summaryContent.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Przetwarzanie...</span>
                </div>
                <p class="mt-3">Przetwarzanie płatności gotówką...</p>
            </div>
        `;
    }
    
    try {
        // Automatycznie wykonaj płatność gotówką (rezerwacja zostanie oznaczona jako PAID)
        const reservationIdInput = document.getElementById('reservationId');
        if (!reservationIdInput || !reservationIdInput.value) {
            throw new Error('Brak ID rezerwacji');
        }
        
        const paymentData = {
            reservationId: parseInt(reservationIdInput.value),
            paymentMethod: 'CASH'
        };
        
        if (isNaN(paymentData.reservationId)) {
            throw new Error('Nieprawidłowe ID rezerwacji');
        }
        
        // Wywołaj API - płatność gotówką automatycznie oznacza rezerwację jako PAID
        const response = await apiRequest('/payments/process', {
            method: 'POST',
            body: JSON.stringify(paymentData)
        });
        
        if (response && response.success) {
            // Sukces - pokaż komunikat sukcesu
            const totalPrice = reservationData.totalPrice ? reservationData.totalPrice.toFixed(2) : '0.00';
            
            if (summaryContent) {
                summaryContent.innerHTML = `
                    <div class="alert alert-success">
                        <h5><i class="bi bi-check-circle me-2"></i>Rezerwacja potwierdzona</h5>
                        <p>Rezerwacja została automatycznie oznaczona jako opłacona.</p>
                        <p class="mb-0">Płatność gotówką należy dokonać w kasie kina przed seansem.</p>
                        <hr>
                        <div class="d-flex justify-content-between mb-3">
                            <span class="fs-5">Do zapłaty:</span>
                            <strong class="fs-4 text-primary">${totalPrice} PLN</strong>
                        </div>
                        <button class="btn btn-primary w-100" onclick="window.location.href='/profile.html'">
                            <i class="bi bi-check-circle me-2"></i>Przejdź do profilu
                        </button>
                    </div>
                `;
            }
        } else {
            // Błąd płatności
            const errorMessage = response && response.message ? response.message : 'Nieznany błąd';
            throw new Error(errorMessage);
        }
    } catch (error) {
        console.error('Error processing cash payment:', error);
        
        // Pokaż błąd
        if (summaryContent) {
            const errorMessage = error.message || 'Wystąpił błąd podczas przetwarzania płatności gotówką.';
            summaryContent.innerHTML = `
                <div class="alert alert-danger">
                    <h5><i class="bi bi-x-circle me-2"></i>Błąd płatności</h5>
                    <p>${errorMessage}</p>
                    <button class="btn btn-secondary w-100 mt-3" onclick="showPaymentMethodSelection()">
                        <i class="bi bi-arrow-left me-2"></i>Wróć do wyboru metody
                    </button>
                </div>
            `;
        }
    }
}

/**
 * Pokazuje formularz płatności dla wybranej metody
 */
function showPaymentForm(method) {
    // Resetuj stan przycisku płatności
    resetPaymentButton();
    
    // Ukryj wszystkie sekcje szczegółów
    const cardDetails = document.getElementById('cardDetails');
    const blikDetails = document.getElementById('blikDetails');
    
    if (cardDetails) cardDetails.style.display = 'none';
    if (blikDetails) blikDetails.style.display = 'none';
    
    // Wyczyść pola formularza
    const cardNumber = document.getElementById('cardNumber');
    const expiryDate = document.getElementById('expiryDate');
    const cvv = document.getElementById('cvv');
    const blikCode = document.getElementById('blikCode');
    
    if (cardNumber) {
        cardNumber.value = '';
        cardNumber.removeAttribute('required');
    }
    if (expiryDate) {
        expiryDate.value = '';
        expiryDate.removeAttribute('required');
    }
    if (cvv) {
        cvv.value = '';
        cvv.removeAttribute('required');
    }
    if (blikCode) {
        blikCode.value = '';
        blikCode.removeAttribute('required');
    }
    
    // Pokaż odpowiednią sekcję
    if (method === 'CREDIT_CARD' || method === 'DEBIT_CARD') {
        if (cardDetails) cardDetails.style.display = 'block';
        // Ustaw required dla pól karty
        if (cardNumber) cardNumber.setAttribute('required', 'required');
        if (expiryDate) expiryDate.setAttribute('required', 'required');
        if (cvv) cvv.setAttribute('required', 'required');
    } else if (method === 'BLIK') {
        if (blikDetails) blikDetails.style.display = 'block';
        if (blikCode) blikCode.setAttribute('required', 'required');
    }
    
    // Ukryj komunikaty sukcesu/błędu na początku
    hidePaymentAlerts();
    
    // Inicjalizuj formatowanie
    initializeFormFormatting();
    
    // Inicjalizuj formularz
    initializePaymentForm();
}

/**
 * Pokazuje ekran wyboru metody płatności
 */
function showPaymentMethodSelection() {
    // Resetuj stan przycisku
    resetPaymentButton();
    
    // Ukryj komunikaty
    hidePaymentAlerts();
    
    // Wyczyść pola formularza
    const cardNumber = document.getElementById('cardNumber');
    const expiryDate = document.getElementById('expiryDate');
    const cvv = document.getElementById('cvv');
    const blikCode = document.getElementById('blikCode');
    
    if (cardNumber) cardNumber.value = '';
    if (expiryDate) expiryDate.value = '';
    if (cvv) cvv.value = '';
    if (blikCode) blikCode.value = '';
    
    const methodSelection = document.getElementById('paymentMethodSelection');
    const summaryPanel = document.getElementById('paymentSummaryPanel');
    
    if (methodSelection) methodSelection.style.display = 'block';
    if (summaryPanel) {
        summaryPanel.classList.remove('active');
        summaryPanel.style.display = 'none';
    }
    
    selectedPaymentMethod = null;
}

/**
 * Wyświetla podsumowanie rezerwacji
 */
function displaySummary() {
    if (!reservationData) {
        console.error('Brak danych rezerwacji do wyświetlenia');
        return;
    }
    
    const summaryContent = document.getElementById('summaryContent');
    if (!summaryContent) {
        console.error('Element summaryContent nie został znaleziony');
        return;
    }
    
    if (!reservationData.seats || !Array.isArray(reservationData.seats)) {
        console.error('Brak lub nieprawidłowa lista miejsc w danych rezerwacji');
        summaryContent.innerHTML = '<div class="alert alert-warning">Brak danych miejsc w rezerwacji</div>';
        return;
    }
    
    const seatsList = reservationData.seats.map(seat => 
        `<div class="d-flex justify-content-between mb-2">
            <span>Rząd ${seat.rowNumber}, Miejsce ${seat.seatNumber}</span>
            <strong>${seat.price ? seat.price.toFixed(2) : '0.00'} PLN</strong>
        </div>`
    ).join('');
    
    const totalPrice = reservationData.totalPrice ? reservationData.totalPrice.toFixed(2) : '0.00';
    
    summaryContent.innerHTML = `
        <div class="mb-3">
            <div class="d-flex justify-content-between mb-2">
                <span>Rezerwacja:</span>
                <strong>#${reservationData.id || 'N/A'}</strong>
            </div>
            <div class="d-flex justify-content-between mb-2">
                <span>Film:</span>
                <strong>${reservationData.movieTitle || 'Brak tytułu'}</strong>
            </div>
            <div class="d-flex justify-content-between mb-2">
                <span>Sala:</span>
                <span>${reservationData.roomNumber || 'N/A'}</span>
            </div>
            <div class="d-flex justify-content-between mb-2">
                <span>Data i godzina:</span>
                <span>${formatDateTime(reservationData.screeningStartTime)}</span>
            </div>
            <div class="d-flex justify-content-between mb-2">
                <span>Liczba miejsc:</span>
                <span>${reservationData.seats.length}</span>
            </div>
        </div>
        <hr>
        ${seatsList}
        <hr>
        <div class="d-flex justify-content-between">
            <span class="fs-5">Do zapłaty:</span>
            <strong class="fs-4 text-primary">${totalPrice} PLN</strong>
        </div>
    `;
    
    // Ustaw kwotę w przycisku
    const buttonAmount = document.getElementById('buttonAmount');
    if (buttonAmount) buttonAmount.textContent = totalPrice;
}

/**
 * Inicjalizuje formatowanie pól formularza
 */
function initializeFormFormatting() {
    // Formatowanie numeru karty
    const cardNumber = document.getElementById('cardNumber');
    if (cardNumber) {
        cardNumber.addEventListener('input', formatCardNumber);
    }
    
    // Formatowanie daty ważności
    const expiryDate = document.getElementById('expiryDate');
    if (expiryDate) {
        expiryDate.addEventListener('input', formatExpiryDate);
    }
}

/**
 * Inicjalizuje formularz płatności
 */
function initializePaymentForm() {
    const paymentForm = document.getElementById('paymentForm');
    if (paymentForm) {
        paymentForm.addEventListener('submit', handlePaymentSubmit);
    } else {
        console.error('Formularz płatności nie został znaleziony');
    }
    
    // Inicjalizuj Bootstrap tooltips (jeśli są)
    try {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    } catch (error) {
        console.warn('Nie udało się zainicjalizować tooltipów:', error);
    }
}

/**
 * Formatuje numer karty (dodaje spacje co 4 cyfry)
 */
function formatCardNumber(event) {
    if (!event || !event.target) return;
    
    try {
        let value = event.target.value.replace(/\s/g, ''); // Usuń wszystkie spacje
        let formatted = value.match(/.{1,4}/g)?.join(' ') || value; // Dodaj spacje co 4 cyfry
        
        // Zapobiegaj nieskończonej pętli - aktualizuj tylko jeśli wartość się zmieniła
        if (event.target.value !== formatted) {
            event.target.value = formatted;
        }
    } catch (error) {
        console.error('Błąd formatowania numeru karty:', error);
    }
}

/**
 * Formatuje datę ważności (MM/RR)
 */
function formatExpiryDate(event) {
    if (!event || !event.target) return;
    
    try {
        let value = event.target.value.replace(/\D/g, ''); // Usuń wszystkie nie-cyfry
        
        if (value.length >= 2) {
            value = value.substring(0, 2) + '/' + value.substring(2, 4);
        }
        
        // Zapobiegaj nieskończonej pętli - aktualizuj tylko jeśli wartość się zmieniła
        if (event.target.value !== value) {
            event.target.value = value;
        }
    } catch (error) {
        console.error('Błąd formatowania daty ważności:', error);
    }
}

/**
 * Obsługuje wysłanie formularza płatności
 */
async function handlePaymentSubmit(event) {
    event.preventDefault();
    
    // Ukryj wszystkie komunikaty przed rozpoczęciem płatności
    hidePaymentAlerts();
    
    if (!reservationData) {
        alert('Brak danych rezerwacji');
        return;
    }
    
    if (!selectedPaymentMethod) {
        alert('Wybierz metodę płatności');
        return;
    }
    
    const reservationIdInput = document.getElementById('reservationId');
    if (!reservationIdInput || !reservationIdInput.value) {
        alert('Brak ID rezerwacji');
        return;
    }
    
    const paymentData = {
        reservationId: parseInt(reservationIdInput.value),
        paymentMethod: selectedPaymentMethod
    };
    
    if (isNaN(paymentData.reservationId)) {
        alert('Nieprawidłowe ID rezerwacji');
        return;
    }
    
    // Dodaj szczegóły w zależności od metody
    if (selectedPaymentMethod === 'CREDIT_CARD' || selectedPaymentMethod === 'DEBIT_CARD') {
        const cardNumber = document.getElementById('cardNumber');
        const expiryDate = document.getElementById('expiryDate');
        const cvv = document.getElementById('cvv');
        
        // Walidacja pól karty
        if (!cardNumber || !cardNumber.value || cardNumber.value.trim() === '') {
            showErrorAlert('Numer karty jest wymagany');
            resetPaymentButton();
            return;
        }
        if (!expiryDate || !expiryDate.value || expiryDate.value.trim() === '') {
            showErrorAlert('Data ważności jest wymagana');
            resetPaymentButton();
            return;
        }
        if (!cvv || !cvv.value || cvv.value.trim() === '') {
            showErrorAlert('CVV jest wymagany');
            resetPaymentButton();
            return;
        }
        
        paymentData.cardNumber = cardNumber.value.replace(/\s/g, '');
        paymentData.expiryDate = expiryDate.value;
        paymentData.cvv = cvv.value;
    } else if (selectedPaymentMethod === 'BLIK') {
        const blikCode = document.getElementById('blikCode');
        if (!blikCode || !blikCode.value || blikCode.value.trim() === '') {
            showErrorAlert('Kod BLIK jest wymagany');
            resetPaymentButton();
            return;
        }
        paymentData.blikCode = blikCode.value;
    }
    
    // Wyłącz przycisk
    const payButton = document.getElementById('payButton');
    const payButtonText = document.getElementById('payButtonText');
    const payButtonLoader = document.getElementById('payButtonLoader');
    
    if (payButton) payButton.disabled = true;
    if (payButtonText) payButtonText.style.display = 'none';
    if (payButtonLoader) payButtonLoader.style.display = 'inline-block';
    
    try {
        const response = await apiRequest('/payments/process', {
            method: 'POST',
            body: JSON.stringify(paymentData)
        });
        
        if (response && response.success) {
            // Sukces - pokaż komunikat sukcesu
            showSuccessAlert('Płatność zakończona sukcesem!');
            
            // Przekieruj po krótkim opóźnieniu
            setTimeout(() => {
                const transactionId = response.transactionId || 'N/A';
                const amount = response.amount ? response.amount.toFixed(2) : '0.00';
                alert(`Płatność zakończona sukcesem!\n\nTransakcja ID: ${transactionId}\nKwota: ${amount} PLN`);
                window.location.href = '/profile.html';
            }, 1000);
        } else {
            // Błąd płatności
            const errorMessage = response && response.message ? response.message : 'Nieznany błąd';
            showErrorAlert(errorMessage);
            resetPaymentButton();
        }
    } catch (error) {
        console.error('Error processing payment:', error);
        
        // Resetuj przycisk
        resetPaymentButton();
        
        // Pokaż błąd
        const errorMessage = error.message || 'Wystąpił nieznany błąd podczas przetwarzania płatności.';
        showErrorAlert(errorMessage);
    }
}

/**
 * Wyświetla komunikat o błędzie
 */
function showErrorAlert(message) {
    const errorAlert = document.getElementById('errorAlert');
    const errorMessageText = document.getElementById('errorMessageText');
    const successAlert = document.getElementById('successAlert');
    
    // Najpierw ukryj sukces, potem pokaż błąd
    if (successAlert) {
        successAlert.style.display = 'none';
    }
    if (errorAlert) {
        errorAlert.style.display = 'flex';
    }
    if (errorMessageText) {
        errorMessageText.textContent = message || 'Wystąpił błąd podczas przetwarzania płatności.';
    }
}

/**
 * Wyświetla komunikat o sukcesie
 */
function showSuccessAlert(message) {
    const errorAlert = document.getElementById('errorAlert');
    const successAlert = document.getElementById('successAlert');
    
    // Najpierw ukryj błąd, potem pokaż sukces
    if (errorAlert) {
        errorAlert.style.display = 'none';
    }
    if (successAlert) {
        successAlert.style.display = 'flex';
        const successText = successAlert.querySelector('div');
        if (successText) {
            successText.textContent = message || 'Płatność zakończona sukcesem!';
        }
    }
}

/**
 * Ukrywa wszystkie komunikaty płatności
 */
function hidePaymentAlerts() {
    const errorAlert = document.getElementById('errorAlert');
    const successAlert = document.getElementById('successAlert');
    
    if (errorAlert) {
        errorAlert.style.display = 'none';
        // Wyczyść tekst błędu
        const errorMessageText = document.getElementById('errorMessageText');
        if (errorMessageText) errorMessageText.textContent = '';
    }
    if (successAlert) {
        successAlert.style.display = 'none';
    }
}

/**
 * Resetuje stan przycisku płatności
 */
function resetPaymentButton() {
    const payButton = document.getElementById('payButton');
    const payButtonText = document.getElementById('payButtonText');
    const payButtonLoader = document.getElementById('payButtonLoader');
    
    if (payButton) {
        payButton.disabled = false;
    }
    if (payButtonText) {
        payButtonText.style.display = 'inline';
    }
    if (payButtonLoader) {
        payButtonLoader.style.display = 'none';
    }
}

/**
 * Formatuje datę i godzinę
 */
function formatDateTime(dateTimeString) {
    if (!dateTimeString) {
        return 'Brak daty';
    }
    
    try {
        const date = new Date(dateTimeString);
        if (isNaN(date.getTime())) {
            return 'Nieprawidłowa data';
        }
        return date.toLocaleString('pl-PL', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        console.error('Błąd formatowania daty:', error);
        return 'Błąd daty';
    }
}

/**
 * Wyświetla błąd
 */
function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
}

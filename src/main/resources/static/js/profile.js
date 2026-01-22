/**
 * Skrypt dla strony profilu użytkownika
 */

document.addEventListener('DOMContentLoaded', async () => {
    const logoutBtn = document.getElementById('logoutBtn');
    
    const adminMoviesLink = document.getElementById('adminMoviesLink');
    const adminRoomsLink = document.getElementById('adminRoomsLink');
    
    // Sprawdź czy użytkownik jest zalogowany
    try {
        const user = await getCurrentUser();
        
        // Wyświetl informacje o użytkowniku
        document.getElementById('username').textContent = user.username;
        document.getElementById('firstName').textContent = user.firstName;
        document.getElementById('lastName').textContent = user.lastName;
        document.getElementById('role').textContent = user.role;
        
        // Pokaż przycisk wylogowania
        logoutBtn.style.display = 'inline-block';
        
        // Jeśli admin, pokaż linki do panelu admina
        if (user.role === 'ADMIN') {
            if (adminMoviesLink) adminMoviesLink.style.display = 'inline-block';
            if (adminRoomsLink) adminRoomsLink.style.display = 'inline-block';
        }
        
        // Załaduj rezerwacje (na razie placeholder)
        loadReservations();
        
        // Załaduj recenzje (na razie placeholder)
        loadReviews();
        
    } catch (error) {
        // Użytkownik nie jest zalogowany - przekieruj do logowania
        console.error('User not logged in:', error);
        window.location.href = '/login.html';
    }
    
    // Obsługa wylogowania
    logoutBtn.addEventListener('click', async () => {
        try {
            await logout();
            window.location.href = '/index.html';
        } catch (error) {
            console.error('Logout error:', error);
            window.location.href = '/index.html';
        }
    });
});

/**
 * Ładuje rezerwacje użytkownika
 * Na razie wyświetla placeholder - będzie zaimplementowane później
 */
async function loadReservations() {
    const container = document.getElementById('reservationsContainer');
    
    try {
        // TODO: Stworzyć endpoint /api/reservations/my
        // const reservations = await apiRequest('/reservations/my', { method: 'GET' });
        
        // Placeholder - na razie nie ma jeszcze endpointu
        container.innerHTML = `
            <p class="info-message">
                Funkcja rezerwacji będzie dostępna wkrótce. 
                Tutaj zobaczysz wszystkie swoje rezerwacje biletów.
            </p>
        `;
    } catch (error) {
        console.error('Error loading reservations:', error);
        container.innerHTML = `
            <p class="error-message">
                Wystąpił błąd podczas ładowania rezerwacji.
            </p>
        `;
    }
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


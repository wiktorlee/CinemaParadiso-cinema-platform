/**
 * Wspólna logika nawigacji dla wszystkich stron
 * Zapewnia spójne wyświetlanie linków nawigacyjnych na wszystkich stronach
 */

async function updateNavigation() {
    const loginLink = document.getElementById('loginLink');
    const registerLink = document.getElementById('registerLink');
    const profileLink = document.getElementById('profileLink');
    const adminMoviesLink = document.getElementById('adminMoviesLink');
    const adminRoomsLink = document.getElementById('adminRoomsLink');
    const adminScreeningsLink = document.getElementById('adminScreeningsLink');
    const logoutBtn = document.getElementById('logoutBtn');
    
    try {
        const user = await getCurrentUser();
        
        // Użytkownik zalogowany
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';
        if (profileLink) profileLink.style.display = 'inline-block';
        if (logoutBtn) logoutBtn.style.display = 'inline-block';
        
        // Sprawdź rolę użytkownika
        if (user.role === 'ADMIN') {
            if (adminMoviesLink) adminMoviesLink.style.display = 'inline-block';
            if (adminRoomsLink) adminRoomsLink.style.display = 'inline-block';
            if (adminScreeningsLink) adminScreeningsLink.style.display = 'inline-block';
        } else {
            if (adminMoviesLink) adminMoviesLink.style.display = 'none';
            if (adminRoomsLink) adminRoomsLink.style.display = 'none';
            if (adminScreeningsLink) adminScreeningsLink.style.display = 'none';
        }
    } catch (error) {
        // Użytkownik nie zalogowany
        if (loginLink) loginLink.style.display = 'inline-block';
        if (registerLink) registerLink.style.display = 'inline-block';
        if (profileLink) profileLink.style.display = 'none';
        if (logoutBtn) logoutBtn.style.display = 'none';
        if (adminMoviesLink) adminMoviesLink.style.display = 'none';
        if (adminRoomsLink) adminRoomsLink.style.display = 'none';
        if (adminScreeningsLink) adminScreeningsLink.style.display = 'none';
    }
}

// Wywołaj przy załadowaniu strony
document.addEventListener('DOMContentLoaded', async () => {
    await updateNavigation();
    
    // Obsługa wylogowania
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            try {
                await logout();
                window.location.href = '/index.html';
            } catch (error) {
                console.error('Logout error:', error);
                // Nawet jeśli wystąpi błąd, przekieruj na stronę główną
                window.location.href = '/index.html';
            }
        });
    }
});


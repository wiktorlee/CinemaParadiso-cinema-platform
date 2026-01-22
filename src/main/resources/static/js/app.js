/**
 * Główny skrypt aplikacji
 * Używany na stronie głównej (index.html)
 */

document.addEventListener('DOMContentLoaded', async () => {
    const userInfoDiv = document.getElementById('userInfo');
    const userDetailsP = document.getElementById('userDetails');
    const notLoggedInDiv = document.getElementById('notLoggedIn');
    const logoutBtn = document.getElementById('logoutBtn');
    
        const loginLink = document.getElementById('loginLink');
        const registerLink = document.getElementById('registerLink');
        const profileLink = document.getElementById('profileLink');
        const adminMoviesLink = document.getElementById('adminMoviesLink');
        const adminRoomsLink = document.getElementById('adminRoomsLink');
        const adminScreeningsLink = document.getElementById('adminScreeningsLink');
    
    // Sprawdź czy użytkownik jest zalogowany
    try {
        const user = await getCurrentUser();
        
        // Użytkownik jest zalogowany
        userInfoDiv.style.display = 'block';
        notLoggedInDiv.style.display = 'none';
        logoutBtn.style.display = 'inline-block';
        profileLink.style.display = 'inline-block';
        loginLink.style.display = 'none';
        registerLink.style.display = 'none';
        
        // Jeśli admin, pokaż linki do panelu admina
        if (user.role === 'ADMIN') {
            if (adminMoviesLink) adminMoviesLink.style.display = 'inline-block';
            if (adminRoomsLink) adminRoomsLink.style.display = 'inline-block';
            if (adminScreeningsLink) adminScreeningsLink.style.display = 'inline-block';
        } else {
            if (adminMoviesLink) adminMoviesLink.style.display = 'none';
            if (adminRoomsLink) adminRoomsLink.style.display = 'none';
            if (adminScreeningsLink) adminScreeningsLink.style.display = 'none';
        }
        
        // Wyświetl informacje o użytkowniku
        userDetailsP.innerHTML = `
            <strong>Username:</strong> ${user.username}<br>
            <strong>Imię:</strong> ${user.firstName}<br>
            <strong>Nazwisko:</strong> ${user.lastName}<br>
            <strong>Rola:</strong> ${user.role}
        `;
        
    } catch (error) {
        // Użytkownik nie jest zalogowany (401 lub inny błąd)
        console.log('User not logged in:', error.message);
        userInfoDiv.style.display = 'none';
        notLoggedInDiv.style.display = 'block';
        logoutBtn.style.display = 'none';
        profileLink.style.display = 'none';
        if (adminMoviesLink) adminMoviesLink.style.display = 'none';
        if (adminRoomsLink) adminRoomsLink.style.display = 'none';
        if (adminScreeningsLink) adminScreeningsLink.style.display = 'none';
        loginLink.style.display = 'inline-block';
        registerLink.style.display = 'inline-block';
    }
    
    // Obsługa wylogowania
    logoutBtn.addEventListener('click', async () => {
        try {
            await logout();
            window.location.reload();
        } catch (error) {
            console.error('Logout error:', error);
            // Nawet jeśli wystąpi błąd, przeładuj stronę
            window.location.reload();
        }
    });
});



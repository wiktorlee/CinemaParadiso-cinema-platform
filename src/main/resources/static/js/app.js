/**
 * Główny skrypt aplikacji
 * Używany na stronie głównej (index.html)
 * Nawigacja jest obsługiwana przez navigation.js
 */

document.addEventListener('DOMContentLoaded', async () => {
    const userInfoDiv = document.getElementById('userInfo');
    const userDetailsP = document.getElementById('userDetails');
    const notLoggedInDiv = document.getElementById('notLoggedIn');
    
    // Inicjalizacja carousel (Bootstrap automatycznie inicjalizuje carousel z data-bs-ride="carousel")
    // Możemy dodać dodatkowe opcje jeśli potrzeba
    
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



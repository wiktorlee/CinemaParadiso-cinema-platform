/**
 * Skrypt dla strony logowania
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    const messageDiv = document.getElementById('message');
    
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Pobierz dane z formularza
        const username = document.getElementById('floatingInput').value;
        const password = document.getElementById('floatingPassword').value;
        
        // Wyłącz przycisk podczas wysyłania
        const submitButton = form.querySelector('button[type="submit"]');
        submitButton.disabled = true;
        const originalText = submitButton.innerHTML;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Logowanie...';
        
        try {
            // Wywołaj API
            const user = await login(username, password);
            
            // Sukces!
            showMessage(messageDiv, `Witaj ${user.firstName}!`, 'success');
            
            // Przekieruj do strony głównej po 1 sekundzie
            setTimeout(() => {
                window.location.href = '/index.html';
            }, 1000);
            
        } catch (error) {
            // Błąd
            let errorMessage = 'Nieprawidłowy username lub hasło.';
            
            if (error.message.includes('Invalid Credentials')) {
                errorMessage = 'Nieprawidłowy username lub hasło.';
            } else if (error.message.includes('User not found')) {
                errorMessage = 'Użytkownik nie istnieje.';
            } else {
                errorMessage = error.message || errorMessage;
            }
            
            showMessage(messageDiv, errorMessage, 'error');
            
            // Włącz przycisk ponownie
            submitButton.disabled = false;
            submitButton.innerHTML = '<i class="bi bi-box-arrow-in-right me-2"></i>Zaloguj się';
        }
    });
});




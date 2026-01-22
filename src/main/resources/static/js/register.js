/**
 * Skrypt dla strony rejestracji
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registerForm');
    const messageDiv = document.getElementById('message');
    
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Pobierz dane z formularza
        const formData = new FormData(form);
        const userData = {
            username: formData.get('username'),
            password: formData.get('password'),
            firstName: formData.get('firstName'),
            lastName: formData.get('lastName'),
        };
        
        // Wyłącz przycisk podczas wysyłania
        const submitButton = form.querySelector('button[type="submit"]');
        submitButton.disabled = true;
        submitButton.textContent = 'Rejestrowanie...';
        
        try {
            // Wywołaj API
            const user = await register(userData);
            
            // Sukces!
            showMessage(messageDiv, `Rejestracja udana! Witaj ${user.firstName}!`, 'success');
            
            // Przekieruj do strony głównej po 2 sekundach
            setTimeout(() => {
                window.location.href = '/index.html';
            }, 2000);
            
        } catch (error) {
            // Błąd
            let errorMessage = 'Wystąpił błąd podczas rejestracji.';
            
            if (error.message.includes('Username already exists')) {
                errorMessage = 'Username już istnieje. Wybierz inny.';
            } else if (error.message.includes('Validation Failed')) {
                errorMessage = 'Sprawdź poprawność wprowadzonych danych.';
            } else {
                errorMessage = error.message || errorMessage;
            }
            
            showMessage(messageDiv, errorMessage, 'error');
            
            // Włącz przycisk ponownie
            submitButton.disabled = false;
            submitButton.textContent = 'Zarejestruj się';
        }
    });
});




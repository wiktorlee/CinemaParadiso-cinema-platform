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
    const adminAuditLogsLink = document.getElementById('adminAuditLogsLink');
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
            // Pokaż dropdown menu dla admina
            const adminDropdown = document.getElementById('adminDropdown');
            if (adminDropdown) adminDropdown.style.display = 'inline-block';
        } else {
            // Ukryj dropdown menu
            const adminDropdown = document.getElementById('adminDropdown');
            if (adminDropdown) adminDropdown.style.display = 'none';
        }
    } catch (error) {
        // Użytkownik nie zalogowany
        if (loginLink) loginLink.style.display = 'inline-block';
        if (registerLink) registerLink.style.display = 'inline-block';
        if (profileLink) profileLink.style.display = 'none';
        if (logoutBtn) logoutBtn.style.display = 'none';
        const adminDropdown = document.getElementById('adminDropdown');
        if (adminDropdown) adminDropdown.style.display = 'none';
    }
}

// Podświetl aktywny link w nawigacji
function setActiveNavLink() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-links a');
    
    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPath || 
            (currentPath === '/' && link.getAttribute('href') === '/index.html')) {
            link.classList.add('active');
        }
    });
}

// Obsługa dropdown menu dla admina
function setupAdminDropdown() {
    const adminDropdownToggle = document.getElementById('adminDropdownToggle');
    const adminDropdownMenu = document.getElementById('adminDropdownMenu');
    
    if (adminDropdownToggle && adminDropdownMenu) {
        // Toggle dropdown
        adminDropdownToggle.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            adminDropdownMenu.classList.toggle('show');
        });
        
        // Zamknij dropdown po kliknięciu poza nim
        document.addEventListener('click', (e) => {
            if (!adminDropdownToggle.contains(e.target) && !adminDropdownMenu.contains(e.target)) {
                adminDropdownMenu.classList.remove('show');
            }
        });
        
        // Podświetl aktywny link w dropdown
        const currentPath = window.location.pathname;
        const dropdownLinks = adminDropdownMenu.querySelectorAll('a');
        dropdownLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === currentPath) {
                link.classList.add('active');
            }
        });
    }
}

// Wywołaj przy załadowaniu strony
document.addEventListener('DOMContentLoaded', async () => {
    await updateNavigation();
    setActiveNavLink();
    setupAdminDropdown();
    
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


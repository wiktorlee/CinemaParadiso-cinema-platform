/**
 * Skrypt dla dashboardu admina
 * Obsługuje sidebar, zwijanie/rozwijanie, aktywny link
 */

document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('dashboardSidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mobileSidebarToggle = document.getElementById('mobileSidebarToggle');
    
    // Toggle sidebar (desktop)
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
            const icon = sidebarToggle.querySelector('i');
            if (sidebar.classList.contains('collapsed')) {
                icon.className = 'bi bi-chevron-right';
            } else {
                icon.className = 'bi bi-chevron-left';
            }
        });
    }
    
    // Toggle sidebar (mobile)
    if (mobileSidebarToggle) {
        mobileSidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
        
        // Zamknij sidebar po kliknięciu poza nim
        document.addEventListener('click', (e) => {
            if (!sidebar.contains(e.target) && !mobileSidebarToggle.contains(e.target)) {
                sidebar.classList.remove('show');
            }
        });
    }
    
    // Podświetl aktywny link w sidebarze
    const currentPath = window.location.pathname;
    const sidebarLinks = sidebar.querySelectorAll('.dashboard-sidebar-nav a');
    sidebarLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
});


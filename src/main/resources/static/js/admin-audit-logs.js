/**
 * Skrypt dla strony logów audytowych (admin)
 */

let currentPage = 0;
const pageSize = 50;

document.addEventListener('DOMContentLoaded', async () => {
    // Sprawdź czy użytkownik jest zalogowany i ma rolę ADMIN
    try {
        const user = await getCurrentUser();
        if (user.role !== 'ADMIN') {
            alert('Brak uprawnień do przeglądania logów audytowych');
            window.location.href = '/index.html';
            return;
        }
    } catch (error) {
        console.error('User not logged in:', error);
        window.location.href = '/login.html';
        return;
    }
    
    await loadAuditLogs();
});

/**
 * Ładuje logi audytowe
 */
async function loadAuditLogs() {
    const container = document.getElementById('logsContainer');
    const paginationContainer = document.getElementById('paginationContainer');
    
    try {
        const response = await apiRequest(`/admin/audit/logs?page=${currentPage}&size=${pageSize}`, {
            method: 'GET'
        });
        
        const logs = response.content || [];
        const totalElements = response.totalElements || 0;
        const totalPages = response.totalPages || 1;
        const hasNext = response.hasNext || false;
        const hasPrevious = response.hasPrevious || false;
        
        if (logs.length === 0) {
            container.innerHTML = '<p class="info-message">Brak logów audytowych.</p>';
            paginationContainer.style.display = 'none';
            return;
        }
        
        // Renderuj logi
        container.innerHTML = `
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>Data i godzina</th>
                            <th>Użytkownik</th>
                            <th>IP</th>
                            <th>Typ encji</th>
                            <th>Nazwa</th>
                            <th>Typ zmiany</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${logs.map(log => `
                            <tr>
                                <td>${formatDateTime(log.timestamp)}</td>
                                <td>${log.username || 'SYSTEM'}</td>
                                <td>${log.ipAddress || '-'}</td>
                                <td><span class="badge bg-info">${log.entityType}</span></td>
                                <td>${log.entityName || `ID: ${log.entityId}`}</td>
                                <td>
                                    ${getRevisionTypeBadge(log.revisionType)}
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        
        // Paginacja
        if (totalPages > 1) {
            displayPagination(totalPages, hasNext, hasPrevious);
            paginationContainer.style.display = 'flex';
        } else {
            paginationContainer.style.display = 'none';
        }
        
    } catch (error) {
        console.error('Error loading audit logs:', error);
        container.innerHTML = `
            <p class="error-message">
                Wystąpił błąd podczas ładowania logów: ${error.message || 'Nieznany błąd'}
            </p>
        `;
        paginationContainer.style.display = 'none';
    }
}

/**
 * Wyświetla paginację
 */
function displayPagination(totalPages, hasNext, hasPrevious) {
    const paginationContainer = document.getElementById('paginationContainer');
    
    let paginationHTML = '<div class="pagination">';
    
    // Przycisk poprzednia strona
    if (hasPrevious) {
        paginationHTML += `<button class="pagination-btn" onclick="goToPage(${currentPage - 1})">‹ Poprzednia</button>`;
    } else {
        paginationHTML += `<button class="pagination-btn" disabled>‹ Poprzednia</button>`;
    }
    
    // Numer strony
    paginationHTML += `<span class="pagination-info">Strona ${currentPage + 1} z ${totalPages}</span>`;
    
    // Przycisk następna strona
    if (hasNext) {
        paginationHTML += `<button class="pagination-btn" onclick="goToPage(${currentPage + 1})">Następna ›</button>`;
    } else {
        paginationHTML += `<button class="pagination-btn" disabled>Następna ›</button>`;
    }
    
    paginationHTML += '</div>';
    paginationContainer.innerHTML = paginationHTML;
}

/**
 * Przechodzi do określonej strony
 */
window.goToPage = function(page) {
    currentPage = page;
    loadAuditLogs();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Odświeża logi
 */
window.refreshLogs = function() {
    currentPage = 0;
    loadAuditLogs();
}

/**
 * Zwraca badge dla typu zmiany
 */
function getRevisionTypeBadge(revisionType) {
    const badges = {
        'Dodano': '<span class="badge bg-success">Dodano</span>',
        'Zmodyfikowano': '<span class="badge bg-warning">Zmodyfikowano</span>',
        'Usunięto': '<span class="badge bg-danger">Usunięto</span>'
    };
    return badges[revisionType] || `<span class="badge bg-secondary">${revisionType}</span>`;
}

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
        minute: '2-digit',
        second: '2-digit'
    });
}



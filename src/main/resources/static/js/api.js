/**
 * API Client - komunikacja z backendem
 * 
 * Wszystkie funkcje do komunikacji z REST API
 */

const API_BASE_URL = '/api';

/**
 * Wykonuje request do API
 * 
 * @param {string} endpoint - endpoint API (np. '/auth/register')
 * @param {object} options - opcje fetch (method, body, headers)
 * @returns {Promise} - Promise z odpowiedzią
 */
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // Wysyła cookies (sesja)
    };
    
    const config = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers,
        },
    };
    
    try {
        const response = await fetch(url, config);
        
        // Sprawdź czy odpowiedź jest OK
        if (!response.ok) {
            // Dla 401 (Unauthorized) - użytkownik nie jest zalogowany
            if (response.status === 401) {
                throw new Error('UNAUTHORIZED');
            }
            
            // Spróbuj pobrać błąd z JSON, jeśli nie ma - użyj domyślnego komunikatu
            const errorData = await response.json().catch(() => ({ 
                message: `HTTP error! status: ${response.status}` 
            }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        // Jeśli odpowiedź jest pusta (np. logout)
        if (response.status === 204 || response.headers.get('content-length') === '0') {
            return null;
        }
        
        return await response.json();
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

/**
 * Rejestracja użytkownika
 * 
 * @param {object} userData - dane użytkownika (username, password, firstName, lastName)
 * @returns {Promise<object>} - dane zarejestrowanego użytkownika
 */
async function register(userData) {
    return apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData),
    });
}

/**
 * Logowanie użytkownika
 * 
 * @param {string} username - username użytkownika
 * @param {string} password - hasło użytkownika
 * @returns {Promise<object>} - dane zalogowanego użytkownika
 */
async function login(username, password) {
    return apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
    });
}

/**
 * Pobiera informacje o zalogowanym użytkowniku
 * 
 * @returns {Promise<object>} - dane użytkownika
 */
async function getCurrentUser() {
    return apiRequest('/auth/me', {
        method: 'GET',
    });
}

/**
 * Wylogowanie użytkownika
 * 
 * @returns {Promise<void>}
 */
async function logout() {
    return apiRequest('/auth/logout', {
        method: 'POST',
    });
}

/**
 * Pobiera wszystkie filmy
 * 
 * @returns {Promise<Array>} - lista filmów
 */
async function getAllMovies() {
    return apiRequest('/movies', {
        method: 'GET',
    });
}

/**
 * Pobiera film po ID
 * 
 * @param {number} id - ID filmu
 * @returns {Promise<object>} - film
 */
async function getMovieById(id) {
    return apiRequest(`/movies/${id}`, {
        method: 'GET',
    });
}

/**
 * Tworzy nowy film (tylko ADMIN)
 * 
 * @param {object} movieData - dane filmu
 * @returns {Promise<object>} - utworzony film
 */
async function createMovie(movieData) {
    return apiRequest('/movies', {
        method: 'POST',
        body: JSON.stringify(movieData),
    });
}

/**
 * Aktualizuje film (tylko ADMIN)
 * 
 * @param {number} id - ID filmu
 * @param {object} movieData - dane do aktualizacji
 * @returns {Promise<object>} - zaktualizowany film
 */
async function updateMovie(id, movieData) {
    return apiRequest(`/movies/${id}`, {
        method: 'PUT',
        body: JSON.stringify(movieData),
    });
}

/**
 * Usuwa film (tylko ADMIN)
 * 
 * @param {number} id - ID filmu
 * @returns {Promise<void>}
 */
async function deleteMovie(id) {
    return apiRequest(`/movies/${id}`, {
        method: 'DELETE',
    });
}

/**
 * Upload okładki filmu (tylko ADMIN)
 * 
 * @param {number} id - ID filmu
 * @param {File} file - plik obrazu
 * @returns {Promise<object>} - zaktualizowany film
 */
async function uploadMoviePoster(id, file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const url = `${API_BASE_URL}/movies/${id}/poster`;
    
    const response = await fetch(url, {
        method: 'POST',
        body: formData,
        credentials: 'include', // Wysyła cookies (sesja)
    });
    
    if (!response.ok) {
        if (response.status === 401) {
            throw new Error('UNAUTHORIZED');
        }
        const errorData = await response.json().catch(() => ({ message: 'Unknown error' }));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
}

/**
 * Pobiera wszystkich użytkowników (tylko ADMIN)
 * 
 * @returns {Promise<Array>} - lista użytkowników
 */
async function getAllUsers() {
    return apiRequest('/admin/users', {
        method: 'GET',
    });
}

/**
 * Zmienia rolę użytkownika (tylko ADMIN)
 * 
 * @param {number} userId - ID użytkownika
 * @param {string} role - nowa rola ('USER' lub 'ADMIN')
 * @returns {Promise<object>} - zaktualizowany użytkownik
 */
async function changeUserRole(userId, role) {
    return apiRequest(`/admin/users/${userId}/role?role=${role}`, {
        method: 'PUT',
    });
}

/**
 * Pobiera wszystkie sale
 * 
 * @returns {Promise<Array>} - lista sal
 */
async function getAllRooms() {
    return apiRequest('/rooms', {
        method: 'GET',
    });
}

/**
 * Pobiera salę po ID
 * 
 * @param {number} id - ID sali
 * @returns {Promise<object>} - sala
 */
async function getRoomById(id) {
    return apiRequest(`/rooms/${id}`, {
        method: 'GET',
    });
}

/**
 * Pobiera wszystkie miejsca w sali
 * 
 * @param {number} roomId - ID sali
 * @returns {Promise<Array>} - lista miejsc
 */
async function getSeatsByRoomId(roomId) {
    return apiRequest(`/rooms/${roomId}/seats`, {
        method: 'GET',
    });
}

/**
 * Tworzy nową salę (tylko ADMIN)
 * 
 * @param {object} roomData - dane sali
 * @returns {Promise<object>} - utworzona sala
 */
async function createRoom(roomData) {
    return apiRequest('/rooms', {
        method: 'POST',
        body: JSON.stringify(roomData),
    });
}

/**
 * Aktualizuje salę (tylko ADMIN)
 * 
 * @param {number} id - ID sali
 * @param {object} roomData - dane do aktualizacji
 * @returns {Promise<object>} - zaktualizowana sala
 */
async function updateRoom(id, roomData) {
    return apiRequest(`/rooms/${id}`, {
        method: 'PUT',
        body: JSON.stringify(roomData),
    });
}

/**
 * Usuwa salę (tylko ADMIN)
 * 
 * @param {number} id - ID sali
 * @returns {Promise<void>}
 */
async function deleteRoom(id) {
    return apiRequest(`/rooms/${id}`, {
        method: 'DELETE',
    });
}

/**
 * Duplikuje salę (tylko ADMIN)
 * 
 * @param {number} id - ID sali źródłowej
 * @param {string} newRoomNumber - numer nowej sali
 * @returns {Promise<object>} - zduplikowana sala
 */
async function duplicateRoom(id, newRoomNumber) {
    return apiRequest(`/rooms/${id}/duplicate`, {
        method: 'POST',
        body: JSON.stringify({ newRoomNumber }),
    });
}

/**
 * Tworzy wiele sal o tym samym formacie (tylko ADMIN)
 * 
 * @param {object} bulkData - dane do masowego tworzenia
 * @returns {Promise<Array>} - lista utworzonych sal
 */
async function createMultipleRooms(bulkData) {
    return apiRequest('/rooms/bulk', {
        method: 'POST',
        body: JSON.stringify(bulkData),
    });
}

/**
 * Pobiera wszystkie seanse (z paginacją)
 * 
 * @param {number} page - numer strony
 * @param {number} size - rozmiar strony
 * @param {string} sort - sortowanie (np. 'startTime,asc')
 * @returns {Promise<object>} - strona z seansami
 */
async function getAllScreenings(page = 0, size = 20, sort = 'startTime,asc') {
    const params = new URLSearchParams({ page, size, sort });
    return apiRequest(`/screenings?${params.toString()}`, { method: 'GET' });
}

/**
 * Pobiera nadchodzące seanse
 * 
 * @param {number} page - numer strony
 * @param {number} size - rozmiar strony
 * @param {string} sort - sortowanie
 * @returns {Promise<object>} - strona z seansami
 */
async function getUpcomingScreenings(page = 0, size = 20, sort = 'startTime,asc') {
    const params = new URLSearchParams({ page, size, sort });
    return apiRequest(`/screenings/upcoming?${params.toString()}`, { method: 'GET' });
}

/**
 * Pobiera seanse w zakresie dat
 * 
 * @param {string} start - data rozpoczęcia (ISO format)
 * @param {string} end - data zakończenia (ISO format)
 * @param {number} page - numer strony
 * @param {number} size - rozmiar strony
 * @returns {Promise<object>} - strona z seansami
 */
async function getScreeningsByDateRange(start, end, page = 0, size = 20) {
    const params = new URLSearchParams({ 
        start: start, 
        end: end, 
        page, 
        size,
        sort: 'startTime,asc'
    });
    return apiRequest(`/screenings/range?${params.toString()}`, { method: 'GET' });
}

/**
 * Pobiera seanse dla danego filmu
 * 
 * @param {number} movieId - ID filmu
 * @param {number} page - numer strony
 * @param {number} size - rozmiar strony
 * @returns {Promise<object>} - strona z seansami
 */
async function getScreeningsByMovie(movieId, page = 0, size = 20) {
    const params = new URLSearchParams({ page, size, sort: 'startTime,asc' });
    return apiRequest(`/screenings/movie/${movieId}?${params.toString()}`, { method: 'GET' });
}

/**
 * Pobiera seans po ID
 * 
 * @param {number} id - ID seansu
 * @returns {Promise<object>} - seans
 */
async function getScreeningById(id) {
    return apiRequest(`/screenings/${id}`, { method: 'GET' });
}

/**
 * Tworzy nowy seans (tylko ADMIN)
 * 
 * @param {object} screeningData - dane seansu
 * @returns {Promise<object>} - utworzony seans
 */
async function createScreening(screeningData) {
    return apiRequest('/screenings', {
        method: 'POST',
        body: JSON.stringify(screeningData),
    });
}

/**
 * Aktualizuje seans (tylko ADMIN)
 * 
 * @param {number} id - ID seansu
 * @param {object} screeningData - dane do aktualizacji
 * @returns {Promise<object>} - zaktualizowany seans
 */
async function updateScreening(id, screeningData) {
    return apiRequest(`/screenings/${id}`, {
        method: 'PUT',
        body: JSON.stringify(screeningData),
    });
}

/**
 * Usuwa seans (tylko ADMIN)
 * 
 * @param {number} id - ID seansu
 * @returns {Promise<void>}
 */
async function deleteScreening(id) {
    return apiRequest(`/screenings/${id}`, {
        method: 'DELETE',
    });
}

/**
 * Pobiera wszystkie harmonogramy
 * 
 * @returns {Promise<Array>} - lista harmonogramów
 */
async function getAllSchedules() {
    return apiRequest('/schedules', { method: 'GET' });
}

/**
 * Pobiera harmonogram po ID
 * 
 * @param {number} id - ID harmonogramu
 * @returns {Promise<object>} - harmonogram
 */
async function getScheduleById(id) {
    return apiRequest(`/schedules/${id}`, { method: 'GET' });
}

/**
 * Tworzy nowy harmonogram (tylko ADMIN)
 * 
 * @param {object} scheduleData - dane harmonogramu
 * @returns {Promise<object>} - utworzony harmonogram
 */
async function createSchedule(scheduleData) {
    return apiRequest('/schedules', {
        method: 'POST',
        body: JSON.stringify(scheduleData),
    });
}

/**
 * Aktualizuje harmonogram (tylko ADMIN)
 * 
 * @param {number} id - ID harmonogramu
 * @param {object} scheduleData - dane do aktualizacji
 * @returns {Promise<object>} - zaktualizowany harmonogram
 */
async function updateSchedule(id, scheduleData) {
    return apiRequest(`/schedules/${id}`, {
        method: 'PUT',
        body: JSON.stringify(scheduleData),
    });
}

/**
 * Usuwa harmonogram (tylko ADMIN)
 * 
 * @param {number} id - ID harmonogramu
 * @returns {Promise<void>}
 */
async function deleteSchedule(id) {
    return apiRequest(`/schedules/${id}`, {
        method: 'DELETE',
    });
}

/**
 * Generuje seanse z harmonogramu (tylko ADMIN)
 * 
 * @param {number} scheduleId - ID harmonogramu
 * @returns {Promise<Array>} - lista utworzonych seansów
 */
async function generateScreeningsFromSchedule(scheduleId) {
    return apiRequest(`/schedules/${scheduleId}/generate`, {
        method: 'POST',
    });
}

/**
 * Wyświetla komunikat na stronie
 * 
 * @param {HTMLElement} element - element HTML do wyświetlenia komunikatu
 * @param {string} message - treść komunikatu
 * @param {string} type - typ komunikatu ('success' lub 'error')
 */
function showMessage(element, message, type = 'success') {
    element.textContent = message;
    element.className = `message ${type}`;
    element.style.display = 'block';
    
    // Ukryj komunikat po 5 sekundach
    setTimeout(() => {
        element.style.display = 'none';
    }, 5000);
}



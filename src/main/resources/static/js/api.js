const API_BASE_URL = '/api';

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
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
        
        if (!response.ok) {
            if (response.status === 401) {
                throw new Error('UNAUTHORIZED');
            }
            
            const errorData = await response.json().catch(() => ({ 
                message: `HTTP error! status: ${response.status}` 
            }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        if (response.status === 204 || response.headers.get('content-length') === '0') {
            return null;
        }
        
        return await response.json();
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

async function register(userData) {
    return apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData),
    });
}

async function login(username, password) {
    return apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
    });
}

async function getCurrentUser() {
    return apiRequest('/auth/me', {
        method: 'GET',
    });
}

async function logout() {
    return apiRequest('/auth/logout', {
        method: 'POST',
    });
}

async function getAllMovies() {
    return apiRequest('/movies', {
        method: 'GET',
    });
}

async function getMovieById(id) {
    return apiRequest(`/movies/${id}`, {
        method: 'GET',
    });
}

async function createMovie(movieData) {
    return apiRequest('/movies', {
        method: 'POST',
        body: JSON.stringify(movieData),
    });
}

async function updateMovie(id, movieData) {
    return apiRequest(`/movies/${id}`, {
        method: 'PUT',
        body: JSON.stringify(movieData),
    });
}

async function deleteMovie(id) {
    return apiRequest(`/movies/${id}`, {
        method: 'DELETE',
    });
}

async function uploadMoviePoster(id, file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const url = `${API_BASE_URL}/movies/${id}/poster`;
    
    const response = await fetch(url, {
        method: 'POST',
        body: formData,
        credentials: 'include',
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

async function getAllUsers() {
    return apiRequest('/admin/users', {
        method: 'GET',
    });
}

async function changeUserRole(userId, role) {
    return apiRequest(`/admin/users/${userId}/role?role=${role}`, {
        method: 'PUT',
    });
}

async function getAllRooms() {
    return apiRequest('/rooms', {
        method: 'GET',
    });
}

async function getRoomById(id) {
    return apiRequest(`/rooms/${id}`, {
        method: 'GET',
    });
}

async function getSeatsByRoomId(roomId) {
    return apiRequest(`/rooms/${roomId}/seats`, {
        method: 'GET',
    });
}

async function createRoom(roomData) {
    return apiRequest('/rooms', {
        method: 'POST',
        body: JSON.stringify(roomData),
    });
}

async function updateRoom(id, roomData) {
    return apiRequest(`/rooms/${id}`, {
        method: 'PUT',
        body: JSON.stringify(roomData),
    });
}

async function deleteRoom(id) {
    return apiRequest(`/rooms/${id}`, {
        method: 'DELETE',
    });
}

async function duplicateRoom(id, newRoomNumber) {
    return apiRequest(`/rooms/${id}/duplicate`, {
        method: 'POST',
        body: JSON.stringify({ newRoomNumber }),
    });
}

async function createMultipleRooms(bulkData) {
    return apiRequest('/rooms/bulk', {
        method: 'POST',
        body: JSON.stringify(bulkData),
    });
}

async function getAllScreenings(page = 0, size = 20, sort = 'startTime,asc') {
    const params = new URLSearchParams({ page, size, sort });
    return apiRequest(`/screenings?${params.toString()}`, { method: 'GET' });
}

async function getUpcomingScreenings(page = 0, size = 20, sort = 'startTime,asc') {
    const params = new URLSearchParams({ page, size, sort });
    return apiRequest(`/screenings/upcoming?${params.toString()}`, { method: 'GET' });
}

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

async function getScreeningsByMovie(movieId, page = 0, size = 20) {
    const params = new URLSearchParams({ page, size, sort: 'startTime,asc' });
    return apiRequest(`/screenings/movie/${movieId}?${params.toString()}`, { method: 'GET' });
}

async function getScreeningById(id) {
    return apiRequest(`/screenings/${id}`, { method: 'GET' });
}

async function createScreening(screeningData) {
    return apiRequest('/screenings', {
        method: 'POST',
        body: JSON.stringify(screeningData),
    });
}

async function updateScreening(id, screeningData) {
    return apiRequest(`/screenings/${id}`, {
        method: 'PUT',
        body: JSON.stringify(screeningData),
    });
}

async function deleteScreening(id) {
    return apiRequest(`/screenings/${id}`, {
        method: 'DELETE',
    });
}

async function getAllSchedules() {
    return apiRequest('/schedules', { method: 'GET' });
}

async function getScheduleById(id) {
    return apiRequest(`/schedules/${id}`, { method: 'GET' });
}

async function createSchedule(scheduleData) {
    return apiRequest('/schedules', {
        method: 'POST',
        body: JSON.stringify(scheduleData),
    });
}

async function updateSchedule(id, scheduleData) {
    return apiRequest(`/schedules/${id}`, {
        method: 'PUT',
        body: JSON.stringify(scheduleData),
    });
}

async function deleteSchedule(id) {
    return apiRequest(`/schedules/${id}`, {
        method: 'DELETE',
    });
}

async function generateScreeningsFromSchedule(scheduleId) {
    return apiRequest(`/schedules/${scheduleId}/generate`, {
        method: 'POST',
    });
}

function showMessage(element, message, type = 'success') {
    element.textContent = message;
    element.className = `message ${type}`;
    element.style.display = 'block';
    
    setTimeout(() => {
        element.style.display = 'none';
    }, 5000);
}



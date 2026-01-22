/**
 * Skrypt do zarządzania seansami w panelu admina
 */

let currentEditingScreeningId = null;
let allMovies = [];
let allRooms = [];

document.addEventListener('DOMContentLoaded', async () => {
    // Sprawdź czy użytkownik jest zalogowany i ma rolę ADMIN
    await checkAuth();
    
    // Załaduj filmy i sale do selectów
    await loadMoviesAndRooms();
    
    // Załaduj listę seansów i harmonogramów
    await loadAdminScreenings();
    await loadAdminSchedules();
    
    // Obsługa formularza seansu
    const form = document.getElementById('screeningForm');
    form.addEventListener('submit', handleScreeningFormSubmit);
    
    // Obsługa przycisku anuluj
    const cancelBtn = document.getElementById('cancelBtn');
    cancelBtn.addEventListener('click', resetScreeningForm);
    
    // Obsługa modala harmonogramu
    const scheduleModal = document.getElementById('scheduleModal');
    const closeScheduleModal = document.getElementById('closeScheduleModal');
    closeScheduleModal.addEventListener('click', () => {
        scheduleModal.style.display = 'none';
    });
    
    const scheduleForm = document.getElementById('scheduleForm');
    scheduleForm.addEventListener('submit', handleScheduleFormSubmit);
    
    window.onclick = function(event) {
        if (event.target === scheduleModal) {
            scheduleModal.style.display = 'none';
        }
    };
});

/**
 * Sprawdza czy użytkownik jest zalogowany i ma rolę ADMIN
 */
async function checkAuth() {
    try {
        const user = await getCurrentUser();
        if (!user || user.role !== 'ADMIN') {
            window.location.href = '/index.html';
        }
    } catch (error) {
        if (error.message === 'UNAUTHORIZED') {
            window.location.href = '/login.html';
        }
    }
}

/**
 * Ładuje filmy i sale do selectów
 */
async function loadMoviesAndRooms() {
    try {
        allMovies = await getAllMovies();
        allRooms = await getAllRooms();
        
        // Wypełnij selecty w formularzu seansu
        const movieSelect = document.getElementById('movieId');
        const roomSelect = document.getElementById('roomId');
        
        movieSelect.innerHTML = '<option value="">Wybierz film...</option>' +
            allMovies.map(movie => `<option value="${movie.id}">${movie.title}</option>`).join('');
        
        roomSelect.innerHTML = '<option value="">Wybierz salę...</option>' +
            allRooms.map(room => `<option value="${room.id}">${room.roomNumber}</option>`).join('');
        
        // Wypełnij selecty w formularzu harmonogramu
        const scheduleMovieSelect = document.getElementById('scheduleMovieId');
        const scheduleRoomSelect = document.getElementById('scheduleRoomId');
        
        scheduleMovieSelect.innerHTML = '<option value="">Wybierz film...</option>' +
            allMovies.map(movie => `<option value="${movie.id}">${movie.title}</option>`).join('');
        
        scheduleRoomSelect.innerHTML = '<option value="">Wybierz salę...</option>' +
            allRooms.map(room => `<option value="${room.id}">${room.roomNumber}</option>`).join('');
        
    } catch (error) {
        console.error('Error loading movies and rooms:', error);
    }
}

/**
 * Ładuje listę seansów do wyświetlenia
 */
async function loadAdminScreenings(dateFilter = null) {
    const container = document.getElementById('adminScreeningsContainer');
    
    try {
        let screenings;
        
        if (dateFilter) {
            // Filtruj po dacie
            const start = new Date(dateFilter);
            start.setHours(0, 0, 0, 0);
            const end = new Date(dateFilter);
            end.setHours(23, 59, 59, 999);
            
            const result = await getScreeningsByDateRange(
                start.toISOString(),
                end.toISOString(),
                0,
                1000 // Duży limit, żeby pokazać wszystkie seanse w danym dniu
            );
            screenings = result.content || [];
        } else {
            // Pobierz nadchodzące seanse
            const result = await getUpcomingScreenings(0, 100);
            screenings = result.content || [];
        }
        
        if (screenings.length === 0) {
            container.innerHTML = '<p>Brak seansów w systemie. Dodaj pierwszy seans używając formularza powyżej.</p>';
            return;
        }
        
        container.innerHTML = screenings.map(screening => {
            const startTime = new Date(screening.startTime);
            const endTime = new Date(screening.endTime);
            const formattedStart = startTime.toLocaleString('pl-PL', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
            const formattedEnd = endTime.toLocaleTimeString('pl-PL', {
                hour: '2-digit',
                minute: '2-digit'
            });
            
            return `
                <div class="admin-screening-card">
                    <div class="screening-info">
                        <h4>${screening.movieTitle}</h4>
                        <p><strong>Sala:</strong> ${screening.roomNumber}</p>
                        <p><strong>Data i godzina:</strong> ${formattedStart} - ${formattedEnd}</p>
                        <p><strong>Cena bazowa:</strong> ${screening.basePrice} PLN</p>
                        ${screening.vipPrice ? `<p><strong>Cena VIP:</strong> ${screening.vipPrice} PLN</p>` : ''}
                        <p><strong>Dostępne miejsca:</strong> ${screening.availableSeats}/${screening.totalSeats}</p>
                    </div>
                    <div class="screening-actions">
                        <button class="btn btn-primary" onclick="editScreening(${screening.id})">
                            Edytuj
                        </button>
                        <button class="btn btn-danger" onclick="deleteScreeningConfirm(${screening.id}, '${screening.movieTitle}')">
                            Usuń
                        </button>
                    </div>
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error loading screenings:', error);
        container.innerHTML = '<p class="error">Błąd podczas ładowania seansów.</p>';
    }
}

/**
 * Ładuje listę harmonogramów
 */
async function loadAdminSchedules() {
    const container = document.getElementById('adminSchedulesContainer');
    
    try {
        const schedules = await getAllSchedules();
        
        if (schedules.length === 0) {
            container.innerHTML = '<p>Brak harmonogramów. Utwórz pierwszy harmonogram używając przycisku powyżej.</p>';
            return;
        }
        
        const dayNames = {
            'MONDAY': 'Poniedziałek',
            'TUESDAY': 'Wtorek',
            'WEDNESDAY': 'Środa',
            'THURSDAY': 'Czwartek',
            'FRIDAY': 'Piątek',
            'SATURDAY': 'Sobota',
            'SUNDAY': 'Niedziela'
        };
        
        container.innerHTML = schedules.map(schedule => {
            const timeStr = schedule.startTime.substring(0, 5); // HH:mm
            
            return `
                <div class="admin-schedule-card">
                    <div class="schedule-info">
                        <h4>${schedule.movieTitle}</h4>
                        <p><strong>Sala:</strong> ${schedule.roomNumber}</p>
                        <p><strong>Dzień:</strong> ${dayNames[schedule.dayOfWeek]}</p>
                        <p><strong>Godzina:</strong> ${timeStr}</p>
                        <p><strong>Okres:</strong> ${schedule.startDate} - ${schedule.endDate}</p>
                        <p><strong>Cena bazowa:</strong> ${schedule.basePrice} PLN</p>
                        ${schedule.vipPrice ? `<p><strong>Cena VIP:</strong> ${schedule.vipPrice} PLN</p>` : ''}
                        <p><strong>Wygenerowane seanse:</strong> ${schedule.generatedScreeningsCount || 0}</p>
                    </div>
                    <div class="schedule-actions">
                        <button class="btn btn-info" onclick="generateScreeningsFromSchedule(${schedule.id})">
                            Generuj seanse
                        </button>
                        <button class="btn btn-primary" onclick="editSchedule(${schedule.id})">
                            Edytuj
                        </button>
                        <button class="btn btn-danger" onclick="deleteScheduleConfirm(${schedule.id})">
                            Usuń
                        </button>
                    </div>
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error loading schedules:', error);
        container.innerHTML = '<p class="error">Błąd podczas ładowania harmonogramów.</p>';
    }
}

/**
 * Obsługuje wysłanie formularza seansu
 */
async function handleScreeningFormSubmit(e) {
    e.preventDefault();
    
    const messageDiv = document.getElementById('message');
    const submitBtn = document.getElementById('submitBtn');
    
    const screeningData = {
        movieId: parseInt(document.getElementById('movieId').value),
        roomId: parseInt(document.getElementById('roomId').value),
        startTime: document.getElementById('startTime').value + ':00', // Dodaj sekundy
        basePrice: parseFloat(document.getElementById('basePrice').value),
        vipPrice: document.getElementById('vipPrice').value ? 
                 parseFloat(document.getElementById('vipPrice').value) : null,
    };
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Zapisywanie...';
        
        let screening;
        
        if (currentEditingScreeningId) {
            // Aktualizuj istniejący seans
            screening = await updateScreening(currentEditingScreeningId, screeningData);
            showMessage(messageDiv, 'Seans został zaktualizowany!', 'success');
        } else {
            // Utwórz nowy seans
            screening = await createScreening(screeningData);
            showMessage(messageDiv, 'Seans został dodany!', 'success');
        }
        
        // Resetuj formularz
        resetScreeningForm();
        
        // Odśwież listę seansów
        await loadAdminScreenings();
        
    } catch (error) {
        console.error('Error saving screening:', error);
        let errorMessage = 'Wystąpił błąd podczas zapisywania seansu.';
        if (error.message.includes('Conflict') || error.message.includes('zajęta')) {
            errorMessage = 'Sala jest już zajęta w tym czasie. Wybierz inny termin.';
        }
        showMessage(messageDiv, errorMessage, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = currentEditingScreeningId ? 'Zaktualizuj seans' : 'Dodaj seans';
    }
}

/**
 * Resetuje formularz seansu
 */
function resetScreeningForm() {
    const form = document.getElementById('screeningForm');
    form.reset();
    currentEditingScreeningId = null;
    
    const formTitle = document.getElementById('formTitle');
    formTitle.textContent = 'Dodaj nowy seans';
    
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.textContent = 'Dodaj seans';
    
    const cancelBtn = document.getElementById('cancelBtn');
    cancelBtn.style.display = 'none';
}

/**
 * Edytuje seans
 */
async function editScreening(screeningId) {
    try {
        const screening = await getScreeningById(screeningId);
        
        // Wypełnij formularz
        document.getElementById('screeningId').value = screening.id;
        document.getElementById('movieId').value = screening.movieId;
        document.getElementById('roomId').value = screening.roomId;
        
        // Formatuj datę dla input datetime-local
        const startTime = new Date(screening.startTime);
        const formattedDate = startTime.toISOString().slice(0, 16);
        document.getElementById('startTime').value = formattedDate;
        
        document.getElementById('basePrice').value = screening.basePrice;
        document.getElementById('vipPrice').value = screening.vipPrice || '';
        
        currentEditingScreeningId = screening.id;
        
        const formTitle = document.getElementById('formTitle');
        formTitle.textContent = 'Edytuj seans';
        
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.textContent = 'Zaktualizuj seans';
        
        const cancelBtn = document.getElementById('cancelBtn');
        cancelBtn.style.display = 'inline-block';
        
        // Przewiń do formularza
        document.querySelector('.admin-form-container').scrollIntoView({ behavior: 'smooth' });
        
    } catch (error) {
        console.error('Error loading screening:', error);
        alert('Błąd podczas ładowania seansu.');
    }
}

/**
 * Potwierdza usunięcie seansu
 */
async function deleteScreeningConfirm(screeningId, movieTitle) {
    if (!confirm(`Czy na pewno chcesz usunąć seans filmu "${movieTitle}"?\n\nUWAGA: Nie można usunąć seansu który ma rezerwacje!`)) {
        return;
    }
    
    try {
        await deleteScreening(screeningId);
        await loadAdminScreenings();
        alert('Seans został usunięty.');
    } catch (error) {
        console.error('Error deleting screening:', error);
        let errorMessage = 'Błąd podczas usuwania seansu.';
        if (error.message.includes('rezerwacje')) {
            errorMessage = 'Nie można usunąć seansu który ma rezerwacje!';
        }
        alert(errorMessage);
    }
}

/**
 * Filtruje seanse po dacie
 */
function filterByDate() {
    const dateFilter = document.getElementById('dateFilter').value;
    if (dateFilter) {
        loadAdminScreenings(dateFilter);
    }
}

/**
 * Czyści filtr daty
 */
function clearDateFilter() {
    document.getElementById('dateFilter').value = '';
    loadAdminScreenings();
}

/**
 * Pokazuje modal do tworzenia harmonogramu
 */
function showScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    modal.style.display = 'block';
}

/**
 * Zamyka modal harmonogramu
 */
function closeScheduleModal() {
    const modal = document.getElementById('scheduleModal');
    modal.style.display = 'none';
    document.getElementById('scheduleForm').reset();
    document.getElementById('scheduleMessage').style.display = 'none';
}

/**
 * Obsługuje wysłanie formularza harmonogramu
 */
async function handleScheduleFormSubmit(e) {
    e.preventDefault();
    
    const messageDiv = document.getElementById('scheduleMessage');
    const submitBtn = e.target.querySelector('button[type="submit"]');
    
    const scheduleData = {
        movieId: parseInt(document.getElementById('scheduleMovieId').value),
        roomId: parseInt(document.getElementById('scheduleRoomId').value),
        dayOfWeek: document.getElementById('dayOfWeek').value,
        startTime: document.getElementById('scheduleStartTime').value + ':00',
        startDate: document.getElementById('scheduleStartDate').value,
        endDate: document.getElementById('scheduleEndDate').value,
        basePrice: parseFloat(document.getElementById('scheduleBasePrice').value),
        vipPrice: document.getElementById('scheduleVipPrice').value ? 
                 parseFloat(document.getElementById('scheduleVipPrice').value) : null,
    };
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Tworzenie...';
        
        const schedule = await createSchedule(scheduleData);
        
        showMessage(messageDiv, 'Harmonogram został utworzony!', 'success');
        
        // Zamknij modal po 2 sekundach
        setTimeout(() => {
            closeScheduleModal();
            loadAdminSchedules();
        }, 2000);
        
    } catch (error) {
        console.error('Error creating schedule:', error);
        let errorMessage = 'Wystąpił błąd podczas tworzenia harmonogramu.';
        if (error.message.includes('Data zakończenia')) {
            errorMessage = 'Data zakończenia musi być późniejsza niż data rozpoczęcia.';
        }
        showMessage(messageDiv, errorMessage, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Utwórz harmonogram';
    }
}

/**
 * Generuje seanse z harmonogramu
 */
async function generateScreeningsFromSchedule(scheduleId) {
    if (!confirm('Czy na pewno chcesz wygenerować seanse z tego harmonogramu?\n\nSystem utworzy seanse dla wszystkich pasujących dat.')) {
        return;
    }
    
    try {
        const screenings = await generateScreeningsFromScheduleAPI(scheduleId);
        alert(`Wygenerowano ${screenings.length} seansów!`);
        await loadAdminScreenings();
        await loadAdminSchedules();
    } catch (error) {
        console.error('Error generating screenings:', error);
        alert('Błąd podczas generowania seansów.');
    }
}

// Funkcja pomocnicza do wywołania API (nie koliduje z funkcją powyżej)
async function generateScreeningsFromScheduleAPI(scheduleId) {
    return apiRequest(`/schedules/${scheduleId}/generate`, {
        method: 'POST',
    });
}

/**
 * Edytuje harmonogram
 */
async function editSchedule(scheduleId) {
    try {
        const schedule = await getScheduleById(scheduleId);
        
        // Wypełnij formularz harmonogramu
        document.getElementById('scheduleMovieId').value = schedule.movieId;
        document.getElementById('scheduleRoomId').value = schedule.roomId;
        document.getElementById('dayOfWeek').value = schedule.dayOfWeek;
        document.getElementById('scheduleStartTime').value = schedule.startTime.substring(0, 5);
        document.getElementById('scheduleStartDate').value = schedule.startDate;
        document.getElementById('scheduleEndDate').value = schedule.endDate;
        document.getElementById('scheduleBasePrice').value = schedule.basePrice;
        document.getElementById('scheduleVipPrice').value = schedule.vipPrice || '';
        
        showScheduleModal();
        
        // TODO: Zmień formularz na tryb edycji (dodaj ukryte pole z ID)
        
    } catch (error) {
        console.error('Error loading schedule:', error);
        alert('Błąd podczas ładowania harmonogramu.');
    }
}

/**
 * Potwierdza usunięcie harmonogramu
 */
async function deleteScheduleConfirm(scheduleId) {
    if (!confirm('Czy na pewno chcesz usunąć ten harmonogram?\n\nUWAGA: Wygenerowane seanse nie zostaną usunięte!')) {
        return;
    }
    
    try {
        await deleteSchedule(scheduleId);
        await loadAdminSchedules();
        alert('Harmonogram został usunięty.');
    } catch (error) {
        console.error('Error deleting schedule:', error);
        alert('Błąd podczas usuwania harmonogramu.');
    }
}


/**
 * Skrypt do zarządzania salami w panelu admina
 */

let currentEditingRoomId = null;

document.addEventListener('DOMContentLoaded', async () => {
    // Sprawdź czy użytkownik jest zalogowany i ma rolę ADMIN
    await checkAuth();
    
    // Załaduj listę sal
    await loadAdminRooms();
    
    // Obsługa formularza
    const form = document.getElementById('roomForm');
    form.addEventListener('submit', handleFormSubmit);
    
    // Obsługa przycisku anuluj
    const cancelBtn = document.getElementById('cancelBtn');
    cancelBtn.addEventListener('click', resetForm);
    
    // Obsługa modala wizualizacji
    const modal = document.getElementById('roomVisualizationModal');
    const closeModal = document.getElementById('closeModal');
    closeModal.addEventListener('click', () => {
        modal.style.display = 'none';
    });
    
    // Obsługa modala masowego tworzenia
    const bulkModal = document.getElementById('bulkCreateModal');
    const closeBulkModal = document.getElementById('closeBulkModal');
    closeBulkModal.addEventListener('click', () => {
        bulkModal.style.display = 'none';
    });
    
    const bulkForm = document.getElementById('bulkCreateForm');
    bulkForm.addEventListener('submit', handleBulkCreateSubmit);
    
    window.onclick = function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
        if (event.target === bulkModal) {
            bulkModal.style.display = 'none';
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
 * Ładuje listę sal do wyświetlenia
 */
async function loadAdminRooms() {
    const container = document.getElementById('adminRoomsContainer');
    if (!container) return;
    
    try {
        // Pokaż loader podczas ładowania sal
        loaderService.showInline('adminRoomsContainer', 'Ładowanie sal...');
        const rooms = await getAllRooms();
        
        if (rooms.length === 0) {
            container.innerHTML = '<p>Brak sal w systemie. Dodaj pierwszą salę używając formularza powyżej.</p>';
            return;
        }
        
        container.innerHTML = rooms.map(room => `
            <div class="admin-room-card">
                <div class="room-info">
                    <h4>${room.roomNumber}</h4>
                    <p><strong>Pojemność:</strong> ${room.capacity} miejsc</p>
                    <p><strong>Rzędy:</strong> ${room.totalRows} × <strong>Miejsca w rzędzie:</strong> ${room.seatsPerRow}</p>
                    ${room.description ? `<p><strong>Opis:</strong> ${room.description}</p>` : ''}
                </div>
                <div class="room-actions">
                    <button class="btn btn-secondary" onclick="showRoomVisualization(${room.id}, '${room.roomNumber}')">
                        Pokaż siatkę miejsc
                    </button>
                    <button class="btn btn-info" onclick="duplicateRoomPrompt(${room.id}, '${room.roomNumber}')">
                        Duplikuj
                    </button>
                    <button class="btn btn-primary" onclick="editRoom(${room.id})">
                        Edytuj
                    </button>
                    <button class="btn btn-danger" onclick="deleteRoomConfirm(${room.id}, '${room.roomNumber}')">
                        Usuń
                    </button>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading rooms:', error);
        container.innerHTML = '<p class="error">Błąd podczas ładowania sal.</p>';
    }
}

/**
 * Obsługuje wysłanie formularza
 */
async function handleFormSubmit(e) {
    e.preventDefault();
    
    const messageDiv = document.getElementById('message');
    const submitBtn = document.getElementById('submitBtn');
    
    const roomData = {
        roomNumber: document.getElementById('roomNumber').value.trim(),
        totalRows: parseInt(document.getElementById('totalRows').value),
        seatsPerRow: parseInt(document.getElementById('seatsPerRow').value),
        description: document.getElementById('description').value.trim() || null,
    };
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Zapisywanie...';
        
        let room;
        
        if (currentEditingRoomId) {
            // Aktualizuj istniejącą salę
            room = await updateRoom(currentEditingRoomId, roomData);
            showMessage(messageDiv, 'Sala została zaktualizowana!', 'success');
        } else {
            // Utwórz nową salę
            room = await createRoom(roomData);
            showMessage(messageDiv, 'Sala została dodana!', 'success');
        }
        
        // Resetuj formularz
        resetForm();
        
        // Odśwież listę sal
        await loadAdminRooms();
        
    } catch (error) {
        console.error('Error saving room:', error);
        let errorMessage = 'Wystąpił błąd podczas zapisywania sali.';
        if (error.message.includes('already exists')) {
            errorMessage = 'Sala o takim numerze już istnieje.';
        }
        showMessage(messageDiv, errorMessage, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = currentEditingRoomId ? 'Zaktualizuj salę' : 'Dodaj salę';
    }
}

/**
 * Resetuje formularz
 */
function resetForm() {
    const form = document.getElementById('roomForm');
    form.reset();
    currentEditingRoomId = null;
    
    const formTitle = document.getElementById('formTitle');
    formTitle.textContent = 'Dodaj nową salę';
    
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.textContent = 'Dodaj salę';
    
    const cancelBtn = document.getElementById('cancelBtn');
    cancelBtn.style.display = 'none';
    
    // Zablokuj edycję pól totalRows i seatsPerRow
    document.getElementById('totalRows').disabled = false;
    document.getElementById('seatsPerRow').disabled = false;
}

/**
 * Edytuje salę
 */
async function editRoom(roomId) {
    try {
        const room = await getRoomById(roomId);
        
        // Wypełnij formularz
        document.getElementById('roomId').value = room.id;
        document.getElementById('roomNumber').value = room.roomNumber;
        document.getElementById('description').value = room.description || '';
        
        // Zablokuj edycję totalRows i seatsPerRow (nie można zmieniać po utworzeniu)
        document.getElementById('totalRows').value = room.totalRows;
        document.getElementById('totalRows').disabled = true;
        document.getElementById('seatsPerRow').value = room.seatsPerRow;
        document.getElementById('seatsPerRow').disabled = true;
        
        currentEditingRoomId = room.id;
        
        const formTitle = document.getElementById('formTitle');
        formTitle.textContent = 'Edytuj salę';
        
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.textContent = 'Zaktualizuj salę';
        
        const cancelBtn = document.getElementById('cancelBtn');
        cancelBtn.style.display = 'inline-block';
        
        // Przewiń do formularza
        document.querySelector('.admin-form-container').scrollIntoView({ behavior: 'smooth' });
        
    } catch (error) {
        console.error('Error loading room:', error);
        notificationService.showError('Błąd podczas ładowania sali.');
    }
}

/**
 * Potwierdza usunięcie sali
 */
async function deleteRoomConfirm(roomId, roomNumber) {
    modalService.confirm(
        'Usuwanie sali',
        `Czy na pewno chcesz usunąć salę "${roomNumber}"?\n\nUWAGA: Ta operacja jest nieodwracalna!`,
        async () => {
            try {
                await deleteRoom(roomId);
                await loadAdminRooms();
                notificationService.showSuccess('Sala została usunięta.');
            } catch (error) {
                console.error('Error deleting room:', error);
                notificationService.showError('Błąd podczas usuwania sali.');
            }
        },
        null,
        {
            confirmText: 'Usuń',
            cancelText: 'Anuluj',
            type: 'danger'
        }
    );
}

/**
 * Pokazuje wizualizację siatki miejsc w sali
 */
async function showRoomVisualization(roomId, roomNumber) {
    const modal = document.getElementById('roomVisualizationModal');
    const modalTitle = document.getElementById('modalRoomTitle');
    const visualization = document.getElementById('roomVisualization');
    
    modalTitle.textContent = `Wizualizacja sali: ${roomNumber}`;
    visualization.innerHTML = '<p>Ładowanie miejsc...</p>';
    modal.style.display = 'block';
    
    try {
        const seats = await getSeatsByRoomId(roomId);
        
        if (seats.length === 0) {
            visualization.innerHTML = '<p>Brak miejsc w tej sali.</p>';
            return;
        }
        
        // Znajdź maksymalne wartości rzędów i miejsc
        const maxRow = Math.max(...seats.map(s => s.rowNumber));
        const maxSeat = Math.max(...seats.map(s => s.seatNumber));
        
        // Utwórz siatkę miejsc
        let gridHTML = '<div class="seat-grid-container">';
        gridHTML += '<div class="screen-indicator">EKRAN</div>';
        gridHTML += '<div class="seat-grid">';
        
        // Sortuj miejsca po rzędzie i numerze miejsca
        const sortedSeats = seats.sort((a, b) => {
            if (a.rowNumber !== b.rowNumber) {
                return a.rowNumber - b.rowNumber;
            }
            return a.seatNumber - b.seatNumber;
        });
        
        // Utwórz mapę miejsc dla szybkiego dostępu
        const seatMap = new Map();
        sortedSeats.forEach(seat => {
            const key = `${seat.rowNumber}-${seat.seatNumber}`;
            seatMap.set(key, seat);
        });
        
        // Generuj siatkę
        for (let row = 1; row <= maxRow; row++) {
            gridHTML += `<div class="seat-row">`;
            gridHTML += `<div class="row-label">Rząd ${row}</div>`;
            
            for (let seat = 1; seat <= maxSeat; seat++) {
                const key = `${row}-${seat}`;
                const seatData = seatMap.get(key);
                
                if (seatData) {
                    const seatClass = seatData.seatType === 'VIP' ? 'seat-vip' : 'seat-standard';
                    const unavailableClass = !seatData.isAvailable ? 'seat-unavailable' : '';
                    gridHTML += `<div class="seat ${seatClass} ${unavailableClass}" title="Rząd ${row}, Miejsce ${seat} (${seatData.seatType})">
                        ${seat}
                    </div>`;
                } else {
                    // Puste miejsce (nie powinno się zdarzyć, ale na wszelki wypadek)
                    gridHTML += `<div class="seat seat-empty"></div>`;
                }
            }
            
            gridHTML += `</div>`;
        }
        
        gridHTML += '</div></div>';
        visualization.innerHTML = gridHTML;
        
    } catch (error) {
        console.error('Error loading seats:', error);
        visualization.innerHTML = '<p class="error">Błąd podczas ładowania miejsc.</p>';
    }
}

/**
 * Pokazuje prompt do duplikowania sali
 */
async function duplicateRoomPrompt(roomId, currentRoomNumber) {
    const newRoomNumber = prompt(`Duplikuj salę "${currentRoomNumber}"\n\nPodaj numer nowej sali:`, `Sala ${Date.now() % 1000}`);
    
    if (!newRoomNumber || newRoomNumber.trim() === '') {
        return;
    }
    
    try {
        const newRoom = await duplicateRoom(roomId, newRoomNumber.trim());
        await loadAdminRooms();
        notificationService.showSuccess(`Sala "${newRoom.roomNumber}" została utworzona!`);
    } catch (error) {
        console.error('Error duplicating room:', error);
        let errorMessage = 'Błąd podczas duplikowania sali.';
        if (error.message.includes('already exists')) {
            errorMessage = 'Sala o takim numerze już istnieje.';
        }
        notificationService.showError(errorMessage);
    }
}

/**
 * Pokazuje modal do masowego tworzenia sal
 */
function showBulkCreateModal() {
    const modal = document.getElementById('bulkCreateModal');
    modal.style.display = 'block';
}

/**
 * Zamyka modal masowego tworzenia sal
 */
function closeBulkCreateModal() {
    const modal = document.getElementById('bulkCreateModal');
    modal.style.display = 'none';
    document.getElementById('bulkCreateForm').reset();
    document.getElementById('bulkMessage').style.display = 'none';
}

/**
 * Obsługuje wysłanie formularza masowego tworzenia sal
 */
async function handleBulkCreateSubmit(e) {
    e.preventDefault();
    
    const messageDiv = document.getElementById('bulkMessage');
    const submitBtn = e.target.querySelector('button[type="submit"]');
    
    const bulkData = {
        roomNumberPrefix: document.getElementById('bulkPrefix').value.trim(),
        startNumber: parseInt(document.getElementById('bulkStartNumber').value),
        count: parseInt(document.getElementById('bulkCount').value),
        totalRows: parseInt(document.getElementById('bulkTotalRows').value),
        seatsPerRow: parseInt(document.getElementById('bulkSeatsPerRow').value),
        description: document.getElementById('bulkDescription').value.trim() || null,
    };
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Tworzenie...';
        
        const createdRooms = await createMultipleRooms(bulkData);
        
        showMessage(messageDiv, `Utworzono ${createdRooms.length} sal!`, 'success');
        
        // Zamknij modal po 2 sekundach
        setTimeout(() => {
            closeBulkCreateModal();
            loadAdminRooms();
        }, 2000);
        
    } catch (error) {
        console.error('Error creating multiple rooms:', error);
        let errorMessage = 'Wystąpił błąd podczas tworzenia sal.';
        if (error.message.includes('already exists')) {
            errorMessage = 'Niektóre sale już istnieją. Sprawdź numery sal.';
        }
        showMessage(messageDiv, errorMessage, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Utwórz sale';
    }
}


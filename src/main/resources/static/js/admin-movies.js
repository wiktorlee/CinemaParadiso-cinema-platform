/**
 * Skrypt dla panelu admina - zarządzanie filmami
 */

let currentEditingMovieId = null;
let selectedFile = null;

document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('movieForm');
    const fileInput = document.getElementById('posterFile');
    const fileUploadArea = document.getElementById('fileUploadArea');
    const filePreview = document.getElementById('filePreview');
    const previewImage = document.getElementById('previewImage');
    const removeFileBtn = document.getElementById('removeFileBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    
    // Sprawdź czy użytkownik jest zalogowany i jest adminem
    try {
        const user = await getCurrentUser();
        if (user.role !== 'ADMIN') {
            window.location.href = '/index.html';
            return;
        }
    } catch (error) {
        window.location.href = '/login.html';
        return;
    }
    
    // Obsługa wylogowania
    logoutBtn.addEventListener('click', async () => {
        try {
            await logout();
            window.location.href = '/index.html';
        } catch (error) {
            console.error('Logout error:', error);
            window.location.href = '/index.html';
        }
    });
    
    // Drag & Drop dla okładki
    fileUploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        fileUploadArea.classList.add('drag-over');
    });
    
    fileUploadArea.addEventListener('dragleave', () => {
        fileUploadArea.classList.remove('drag-over');
    });
    
    fileUploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        fileUploadArea.classList.remove('drag-over');
        
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileSelect(files[0]);
        }
    });
    
    // Kliknięcie w obszar uploadu
    fileUploadArea.addEventListener('click', () => {
        fileInput.click();
    });
    
    // Wybór pliku przez input
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleFileSelect(e.target.files[0]);
        }
    });
    
    // Usuń wybrany plik
    removeFileBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        selectedFile = null;
        fileInput.value = '';
        filePreview.style.display = 'none';
        fileUploadArea.querySelector('.file-upload-content').style.display = 'block';
    });
    
    // Anuluj edycję
    cancelBtn.addEventListener('click', () => {
        resetForm();
    });
    
    // Obsługa formularza
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await handleFormSubmit();
    });
    
    // Załaduj listę filmów
    await loadAdminMovies();
});

/**
 * Obsługuje wybór pliku
 */
function handleFileSelect(file) {
    // Walidacja typu pliku
    if (!file.type.startsWith('image/')) {
        alert('Wybierz plik obrazu (PNG, JPG, etc.)');
        return;
    }
    
    // Walidacja rozmiaru (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
        alert('Plik jest za duży. Maksymalny rozmiar: 5MB');
        return;
    }
    
    selectedFile = file;
    
    // Pokaż podgląd
    const reader = new FileReader();
    reader.onload = (e) => {
        previewImage.src = e.target.result;
        filePreview.style.display = 'block';
        fileUploadArea.querySelector('.file-upload-content').style.display = 'none';
    };
    reader.readAsDataURL(file);
}

/**
 * Obsługuje wysłanie formularza
 */
async function handleFormSubmit() {
    const messageDiv = document.getElementById('message');
    const submitBtn = document.getElementById('submitBtn');
    const form = document.getElementById('movieForm');
    
    // Pobierz dane z formularza
    const movieData = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        genre: document.getElementById('genre').value,
        director: document.getElementById('director').value,
        durationMinutes: parseInt(document.getElementById('durationMinutes').value),
        releaseDate: document.getElementById('releaseDate').value || null,
        year: document.getElementById('year').value ? parseInt(document.getElementById('year').value) : null,
    };
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Zapisywanie...';
        
        let movie;
        
        if (currentEditingMovieId) {
            // Aktualizuj istniejący film
            movie = await updateMovie(currentEditingMovieId, movieData);
            showMessage(messageDiv, 'Film został zaktualizowany!', 'success');
        } else {
            // Utwórz nowy film
            movie = await createMovie(movieData);
            showMessage(messageDiv, 'Film został dodany!', 'success');
            currentEditingMovieId = movie.id;
        }
        
        // Upload okładki jeśli wybrano plik
        if (selectedFile) {
            try {
                await uploadMoviePoster(movie.id, selectedFile);
                showMessage(messageDiv, 'Film i okładka zostały zapisane!', 'success');
            } catch (error) {
                console.error('Error uploading poster:', error);
                showMessage(messageDiv, 'Film został zapisany, ale wystąpił błąd podczas uploadu okładki.', 'error');
            }
        }
        
    // Resetuj formularz tylko jeśli nie edytujemy
    if (!currentEditingMovieId) {
        resetForm();
    } else {
        // Jeśli edytujemy, tylko wyczyść wybór pliku
        selectedFile = null;
        document.getElementById('posterFile').value = '';
    }
    
    // Odśwież listę filmów
    await loadAdminMovies();
        
    } catch (error) {
        console.error('Error saving movie:', error);
        let errorMessage = 'Wystąpił błąd podczas zapisywania filmu.';
        if (error.message.includes('already exists')) {
            errorMessage = 'Film o takim tytule już istnieje.';
        }
        showMessage(messageDiv, errorMessage, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = currentEditingMovieId ? 'Zaktualizuj film' : 'Dodaj film';
    }
}

/**
 * Resetuje formularz
 */
function resetForm() {
    const form = document.getElementById('movieForm');
    form.reset();
    currentEditingMovieId = null;
    selectedFile = null;
    document.getElementById('posterFile').value = '';
    document.getElementById('filePreview').style.display = 'none';
    document.getElementById('fileUploadArea').querySelector('.file-upload-content').style.display = 'block';
    document.getElementById('formTitle').textContent = 'Dodaj nowy film';
    document.getElementById('submitBtn').textContent = 'Dodaj film';
    document.getElementById('cancelBtn').style.display = 'none';
}

/**
 * Edytuje film
 */
async function editMovie(id) {
    try {
        const movie = await getMovieById(id);
        
        // Wypełnij formularz
        document.getElementById('movieId').value = movie.id;
        document.getElementById('title').value = movie.title || '';
        document.getElementById('description').value = movie.description || '';
        document.getElementById('genre').value = movie.genre || '';
        document.getElementById('director').value = movie.director || '';
        document.getElementById('durationMinutes').value = movie.durationMinutes || '';
        document.getElementById('releaseDate').value = movie.releaseDate || '';
        document.getElementById('year').value = movie.year || '';
        
        // Jeśli film ma okładkę, pokaż ją (ale nie ustaw selectedFile - użytkownik może wybrać nową)
        if (movie.posterPath) {
            previewImage.src = movie.posterPath;
            filePreview.style.display = 'block';
            fileUploadArea.querySelector('.file-upload-content').style.display = 'none';
        } else {
            filePreview.style.display = 'none';
            fileUploadArea.querySelector('.file-upload-content').style.display = 'block';
        }
        
        currentEditingMovieId = id;
        document.getElementById('formTitle').textContent = 'Edytuj film';
        document.getElementById('submitBtn').textContent = 'Zaktualizuj film';
        document.getElementById('cancelBtn').style.display = 'inline-block';
        
        // Przewiń do formularza
        document.querySelector('.admin-form-container').scrollIntoView({ behavior: 'smooth' });
        
    } catch (error) {
        console.error('Error loading movie:', error);
        alert('Wystąpił błąd podczas ładowania filmu.');
    }
}

/**
 * Usuwa film
 */
async function deleteMovieHandler(id) {
    if (!confirm('Czy na pewno chcesz usunąć ten film?')) {
        return;
    }
    
    try {
        await deleteMovie(id);
        await loadAdminMovies();
        alert('Film został usunięty.');
    } catch (error) {
        console.error('Error deleting movie:', error);
        alert('Wystąpił błąd podczas usuwania filmu.');
    }
}

/**
 * Ładuje listę filmów do zarządzania
 */
async function loadAdminMovies() {
    const container = document.getElementById('adminMoviesContainer');
    
    try {
        const movies = await getAllMovies();
        
        if (movies.length === 0) {
            container.innerHTML = '<p>Brak filmów w systemie.</p>';
            return;
        }
        
        container.innerHTML = `
            <div class="admin-movies-grid">
                ${movies.map(movie => `
                    <div class="admin-movie-card">
                        ${movie.posterPath ? 
                            `<img src="${movie.posterPath}" alt="${movie.title}" class="admin-movie-poster">` :
                            `<div class="admin-movie-poster-placeholder">Brak okładki</div>`
                        }
                        <div class="admin-movie-info">
                            <h4>${movie.title}</h4>
                            ${movie.director ? `<p>Reżyser: ${movie.director}</p>` : ''}
                            ${movie.durationMinutes ? `<p>Czas: ${movie.durationMinutes} min</p>` : ''}
                        </div>
                        <div class="admin-movie-actions">
                            <button class="btn btn-small btn-primary" onclick="editMovie(${movie.id})">Edytuj</button>
                            <button class="btn btn-small btn-danger" onclick="deleteMovieHandler(${movie.id})">Usuń</button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
        
    } catch (error) {
        console.error('Error loading movies:', error);
        container.innerHTML = '<p class="error-message">Wystąpił błąd podczas ładowania filmów.</p>';
    }
}


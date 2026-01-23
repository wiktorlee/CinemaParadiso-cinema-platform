/**
 * Skrypt dla dashboardu statystyk admina
 * Ładuje dane i rysuje wykresy
 */

let dailyRevenueChart = null;
let paymentMethodChart = null;
let monthlyRevenueChart = null;
let dailyReservationsChart = null;

document.addEventListener('DOMContentLoaded', async () => {
    // Sprawdź czy użytkownik jest zalogowany i ma uprawnienia admina
    try {
        const user = await getCurrentUser();
        if (!user || user.role !== 'ADMIN') {
            window.location.href = '/index.html';
            return;
        }
    } catch (error) {
        console.error('Błąd sprawdzania użytkownika:', error);
        window.location.href = '/login.html';
        return;
    }
    
    // Załaduj statystyki
    await loadStatistics();
    
    // Obsługa przycisku odświeżania
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', async () => {
            refreshBtn.disabled = true;
            refreshBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Odświeżanie...';
            await loadStatistics();
            refreshBtn.disabled = false;
            refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Odśwież';
        });
    }
});

/**
 * Ładuje wszystkie statystyki z API
 */
async function loadStatistics() {
    try {
        const stats = await apiRequest('/admin/statistics', { method: 'GET' });
        
        // Aktualizuj karty statystyk
        updateStatisticsCards(stats);
        
        // Rysuj wykresy
        drawCharts(stats);
        
    } catch (error) {
        console.error('Błąd ładowania statystyk:', error);
        showError('Nie udało się załadować statystyk. Spróbuj odświeżyć stronę.');
    }
}

/**
 * Aktualizuje karty ze statystykami
 */
function updateStatisticsCards(stats) {
    // Całkowity zysk
    const totalRevenueEl = document.getElementById('totalRevenue');
    if (totalRevenueEl && stats.totalRevenue !== undefined) {
        totalRevenueEl.textContent = formatCurrency(stats.totalRevenue);
    }
    
    // Opłacone rezerwacje
    const paidReservationsEl = document.getElementById('paidReservations');
    if (paidReservationsEl && stats.paidReservations !== undefined) {
        paidReservationsEl.textContent = stats.paidReservations;
    }
    
    // Wszystkie rezerwacje
    const totalReservationsEl = document.getElementById('totalReservations');
    if (totalReservationsEl && stats.totalReservations !== undefined) {
        totalReservationsEl.textContent = stats.totalReservations;
    }
    
    // Zarezerwowane miejsca
    const totalReservedSeatsEl = document.getElementById('totalReservedSeats');
    if (totalReservedSeatsEl && stats.totalReservedSeats !== undefined) {
        totalReservedSeatsEl.textContent = stats.totalReservedSeats;
    }
    
    // Wszystkie oceny
    const totalRatingsEl = document.getElementById('totalRatings');
    if (totalRatingsEl && stats.totalRatings !== undefined) {
        totalRatingsEl.textContent = stats.totalRatings;
    }
    
    // Wszystkie recenzje
    const totalReviewsEl = document.getElementById('totalReviews');
    if (totalReviewsEl && stats.totalReviews !== undefined) {
        totalReviewsEl.textContent = stats.totalReviews;
    }
    
    // Średnia ocena
    const averageRatingEl = document.getElementById('averageRating');
    if (averageRatingEl && stats.averageRating !== undefined && stats.averageRating !== null) {
        averageRatingEl.textContent = stats.averageRating.toFixed(1) + ' / 5.0';
    } else if (averageRatingEl) {
        averageRatingEl.textContent = 'Brak danych';
    }
}

/**
 * Rysuje wszystkie wykresy
 */
function drawCharts(stats) {
    // Wykres dziennych zysków (ostatnie 7 dni)
    if (stats.dailyRevenueLast7Days) {
        drawDailyRevenueChart(stats.dailyRevenueLast7Days);
    }
    
    // Wykres zysków według metody płatności
    if (stats.revenueByPaymentMethod) {
        drawPaymentMethodChart(stats.revenueByPaymentMethod);
    }
    
    // Wykres miesięcznych zysków (ostatnie 12 miesięcy)
    if (stats.monthlyRevenue) {
        drawMonthlyRevenueChart(stats.monthlyRevenue);
    }
    
    // Wykres dziennych rezerwacji (ostatnie 7 dni)
    if (stats.dailyReservationsLast7Days) {
        drawDailyReservationsChart(stats.dailyReservationsLast7Days);
    }
}

/**
 * Rysuje wykres dziennych zysków
 */
function drawDailyRevenueChart(dailyRevenue) {
    const ctx = document.getElementById('dailyRevenueChart');
    if (!ctx) return;
    
    // Sortuj klucze chronologicznie
    const sortedKeys = Object.keys(dailyRevenue).sort();
    const labels = sortedKeys.map(date => formatDate(date));
    const data = sortedKeys.map(key => parseFloat(dailyRevenue[key]));
    
    if (dailyRevenueChart) {
        dailyRevenueChart.destroy();
    }
    
    dailyRevenueChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Zyski (PLN)',
                data: data,
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                tension: 0.1,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return value.toFixed(2) + ' PLN';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Rysuje wykres zysków według metody płatności
 */
function drawPaymentMethodChart(revenueByMethod) {
    const ctx = document.getElementById('paymentMethodChart');
    if (!ctx) return;
    
    const labels = Object.keys(revenueByMethod).map(method => formatPaymentMethod(method));
    const data = Object.values(revenueByMethod).map(val => parseFloat(val));
    
    // Kolory dla różnych metod płatności
    const colors = [
        'rgba(255, 99, 132, 0.8)',
        'rgba(54, 162, 235, 0.8)',
        'rgba(255, 206, 86, 0.8)',
        'rgba(75, 192, 192, 0.8)',
        'rgba(153, 102, 255, 0.8)',
        'rgba(255, 159, 64, 0.8)'
    ];
    
    if (paymentMethodChart) {
        paymentMethodChart.destroy();
    }
    
    paymentMethodChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors.slice(0, labels.length),
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            return label + ': ' + formatCurrency(value);
                        }
                    }
                }
            }
        }
    });
}

/**
 * Rysuje wykres miesięcznych zysków
 */
function drawMonthlyRevenueChart(monthlyRevenue) {
    const ctx = document.getElementById('monthlyRevenueChart');
    if (!ctx) return;
    
    // Sortuj klucze chronologicznie
    const sortedKeys = Object.keys(monthlyRevenue).sort();
    const labels = sortedKeys.map(month => formatMonth(month));
    const data = sortedKeys.map(key => parseFloat(monthlyRevenue[key]));
    
    if (monthlyRevenueChart) {
        monthlyRevenueChart.destroy();
    }
    
    monthlyRevenueChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Zyski (PLN)',
                data: data,
                backgroundColor: 'rgba(54, 162, 235, 0.8)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return value.toFixed(2) + ' PLN';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Rysuje wykres dziennych rezerwacji
 */
function drawDailyReservationsChart(dailyReservations) {
    const ctx = document.getElementById('dailyReservationsChart');
    if (!ctx) return;
    
    // Sortuj klucze chronologicznie
    const sortedKeys = Object.keys(dailyReservations).sort();
    const labels = sortedKeys.map(date => formatDate(date));
    const data = sortedKeys.map(key => parseInt(dailyReservations[key]));
    
    if (dailyReservationsChart) {
        dailyReservationsChart.destroy();
    }
    
    dailyReservationsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Liczba rezerwacji',
                data: data,
                backgroundColor: 'rgba(255, 159, 64, 0.8)',
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

/**
 * Formatuje kwotę jako walutę
 */
function formatCurrency(amount) {
    if (typeof amount === 'string') {
        amount = parseFloat(amount);
    }
    return new Intl.NumberFormat('pl-PL', {
        style: 'currency',
        currency: 'PLN',
        minimumFractionDigits: 2
    }).format(amount);
}

/**
 * Formatuje datę (YYYY-MM-DD -> DD.MM)
 */
function formatDate(dateString) {
    // dateString jest w formacie "YYYY-MM-DD"
    const [year, month, day] = dateString.split('-');
    return `${day}.${month}`;
}

/**
 * Formatuje miesiąc (YYYY-MM -> MM/YYYY)
 * Dodatkowo sortuje klucze, żeby były w odpowiedniej kolejności
 */
function formatMonth(monthString) {
    const [year, month] = monthString.split('-');
    // Formatuj jako "MM/YYYY" dla lepszej czytelności
    const monthNames = ['Sty', 'Lut', 'Mar', 'Kwi', 'Maj', 'Cze', 'Lip', 'Sie', 'Wrz', 'Paź', 'Lis', 'Gru'];
    const monthIndex = parseInt(month) - 1;
    return `${monthNames[monthIndex]} ${year}`;
}

/**
 * Formatuje nazwę metody płatności
 */
function formatPaymentMethod(method) {
    const methodNames = {
        'CREDIT_CARD': 'Karta kredytowa',
        'DEBIT_CARD': 'Karta debetowa',
        'BLIK': 'BLIK',
        'PAYPAL': 'PayPal',
        'CASH': 'Gotówka',
        'MOCK': 'Symulacja'
    };
    return methodNames[method] || method;
}

/**
 * Wyświetla błąd
 */
function showError(message) {
    console.error(message);
    notificationService.showError(message);
}


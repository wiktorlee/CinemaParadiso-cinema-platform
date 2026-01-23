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
    
    const labels = Object.keys(dailyRevenue).map(date => formatDate(date));
    const data = Object.values(dailyRevenue).map(val => parseFloat(val));
    
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
    
    const labels = Object.keys(monthlyRevenue).map(month => formatMonth(month));
    const data = Object.values(monthlyRevenue).map(val => parseFloat(val));
    
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
    
    const labels = Object.keys(dailyReservations).map(date => formatDate(date));
    const data = Object.values(dailyReservations).map(val => parseInt(val));
    
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
 * Formatuje datę
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', {
        day: '2-digit',
        month: '2-digit'
    });
}

/**
 * Formatuje miesiąc (YYYY-MM -> MM/YYYY)
 */
function formatMonth(monthString) {
    const [year, month] = monthString.split('-');
    return `${month}/${year}`;
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
    // Można dodać toast notification lub alert
    console.error(message);
    alert(message);
}


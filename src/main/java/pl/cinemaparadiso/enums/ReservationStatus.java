package pl.cinemaparadiso.enums;

/**
 * Status rezerwacji
 * 
 * PENDING_PAYMENT - rezerwacja oczekuje na płatność
 * PAID - rezerwacja opłacona (aktywna)
 * CANCELLED - anulowana rezerwacja
 * PAYMENT_FAILED - płatność nie powiodła się
 */
public enum ReservationStatus {
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    PAYMENT_FAILED
}


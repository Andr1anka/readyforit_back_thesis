package com.andr1anka.readyforit.model;

/**
 * Статус скарги для обробки адміністратором (п.7).
 */
public enum ComplaintStatus {
    OPEN,        // подана, очікує розгляду
    RESOLVED,    // розглянута, вжито заходів
    REJECTED     // відхилена
}

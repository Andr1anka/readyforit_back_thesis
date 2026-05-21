package com.andr1anka.readyforit.model;

public enum VerificationStatus {
    NONE,         // ще не подавав
    PENDING,      // йде обробка
    VERIFIED,     // пройшов
    REJECTED,     // автоматично відхилено
    ESCALATED     // користувач передав на ручний розгляд адміну
}

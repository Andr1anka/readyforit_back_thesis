package com.andr1anka.readyforit.model;

public enum PaymentStatus {
    CREATED,     // створено замовлення в LiqPay
    SUCCESS,     // оплачено, баланс поповнено
    FAILURE,     // невдала спроба
    REVERSED,    // повернення
    SANDBOX      // тестовий платіж
}
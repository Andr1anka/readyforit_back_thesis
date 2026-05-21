package com.andr1anka.readyforit.model;

public enum InterviewerRequestStatus {
    NONE,       // ще не подавав
    PENDING,    // заявку подано, чекає на адміна
    APPROVED,   // схвалено → роль USER міняється на INTERVIEWER
    REJECTED    // відхилено
}

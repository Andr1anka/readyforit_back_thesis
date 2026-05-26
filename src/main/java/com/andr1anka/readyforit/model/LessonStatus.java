package com.andr1anka.readyforit.model;

/**
 * Життєвий цикл уроку щодо коштів (escrow):
 * BOOKED      — студент записався, кошти списані з його балансу й "заморожені" на застосунку;
 * COMPLETED   — урок проведено й інтерв'юер написав рецензію → кошти перераховано інтерв'юеру;
 * CANCELLED   — урок скасовано → кошти повернено студенту.
 */
public enum LessonStatus {
    BOOKED,
    COMPLETED,
    CANCELLED
}

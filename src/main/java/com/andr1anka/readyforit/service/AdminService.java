package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.*;

import java.util.List;

/**
 * Панель адміністратора (Feature 7): скарги, користувачі, заявки інтерв'юерів,
 * верифікація.
 */
public interface AdminService {

    // --- Скарги ---
    List<ComplaintViewDTO> getComplaints(String status);
    ComplaintViewDTO resolveComplaint(Long complaintId, boolean accept, String adminComment);

    // --- Користувачі ---
    List<AdminUserDTO> getUsers();
    AdminUserDTO setBlocked(Long userId, boolean blocked);

    // --- Заявки інтерв'юерів ---
    List<AdminRequestDTO> getPendingRequests();
    AdminRequestDTO decideRequest(Long requestId, boolean approve, String adminComment);

    // --- Верифікація (ескальовані вручну) ---
    List<AdminUserDTO> getVerificationQueue();
    AdminUserDTO decideVerification(Long userId, boolean approve);
}

package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.AuthResponseDTO;
import com.andr1anka.readyforit.dto.LoginRequestDTO;
import com.andr1anka.readyforit.dto.RegisterRequestDTO;

public interface AuthService {
    void register(RegisterRequestDTO request);
    void verifyEmail(String token);
    AuthResponseDTO login(LoginRequestDTO request);

    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);}

package com.andr1anka.readyforit.service;

public interface JwtService {
    String generateToken(String email);
    String extractUsername(String token);
    boolean isTokenValid(String token, String email);
}

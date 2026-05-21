package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.ChangePasswordRequestDTO;
import com.andr1anka.readyforit.dto.UpdateProfileRequestDTO;
import com.andr1anka.readyforit.dto.UserProfileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    UserProfileResponseDTO getMyProfile(String email);

    UserProfileResponseDTO updateProfile(String email, UpdateProfileRequestDTO dto);

    void changePassword(String email, ChangePasswordRequestDTO dto);

    UserProfileResponseDTO uploadAvatar(String email, MultipartFile file);

    byte[] getAvatarBytes(Long userId, String requesterEmail);
}
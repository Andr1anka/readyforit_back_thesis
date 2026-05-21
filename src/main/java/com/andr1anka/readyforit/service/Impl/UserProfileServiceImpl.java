package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.ChangePasswordRequestDTO;
import com.andr1anka.readyforit.dto.UpdateProfileRequestDTO;
import com.andr1anka.readyforit.dto.UserProfileResponseDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.service.FileStorageService;
import com.andr1anka.readyforit.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
        return toDto(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateProfile(String email, UpdateProfileRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName().trim());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName().trim());
        if (dto.getAge() != null) user.setAge(dto.getAge());

        userRepository.save(user);
        return toDto(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Поточний пароль невірний");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Новий пароль не повинен співпадати з поточним");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        // Видаляємо попередній, якщо був
        if (user.getPicture() != null) {
            fileStorageService.delete(fileStorageService.getAvatarsBucket(), user.getPicture());
        }

        String key = fileStorageService.uploadAvatar(file, user.getId());
        user.setPicture(key);
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getAvatarBytes(Long userId, String requesterEmail) {
        // Аватари — публічні в межах системи (будь-який залогінений може бачити)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
        if (user.getPicture() == null) {
            throw new BadRequestException("Аватар не завантажено");
        }
        return fileStorageService.getRawBytes(fileStorageService.getAvatarsBucket(), user.getPicture());
    }

    private UserProfileResponseDTO toDto(User user) {
        boolean hasAvatar = user.getPicture() != null;
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .age(user.getAge())
                .email(user.getEmail())
                .role(user.getRole())
                .rank(user.getRank())
                .balance(user.getBalance() == null ? java.math.BigDecimal.ZERO : user.getBalance())
                .hasCustomAvatar(hasAvatar)
                .avatarUrl(hasAvatar ? "/api/user/me/avatar?u=" + user.getId() : null)
                .initials(buildInitials(user))
                .isVerificated(user.isVerificated())
                .verificationStatus(user.getVerificationStatus())
                .interviewerRequestStatus(user.getInterviewerRequestStatus())
                .build();
    }

    private String buildInitials(User user) {
        String f = user.getFirstName() == null || user.getFirstName().isBlank() ? "" :
                String.valueOf(user.getFirstName().trim().charAt(0));
        String l = user.getLastName() == null || user.getLastName().isBlank() ? "" :
                String.valueOf(user.getLastName().trim().charAt(0));
        return (f + l).toUpperCase();
    }
}
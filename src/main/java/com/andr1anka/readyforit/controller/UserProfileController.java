package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.ChangePasswordRequestDTO;
import com.andr1anka.readyforit.dto.UpdateProfileRequestDTO;
import com.andr1anka.readyforit.dto.UserProfileResponseDTO;
import com.andr1anka.readyforit.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userProfileService.getMyProfile(principal.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequestDTO dto
    ) {
        return ResponseEntity.ok(userProfileService.updateProfile(principal.getUsername(), dto));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequestDTO dto
    ) {
        userProfileService.changePassword(principal.getUsername(), dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponseDTO> uploadAvatar(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(userProfileService.uploadAvatar(principal.getUsername(), file));
    }

    /** Віддає аватар будь-якого користувача (потрібна автентифікація). */
    @GetMapping("/me/avatar")
    public ResponseEntity<byte[]> getAvatar(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("u") Long userId
    ) {
        byte[] bytes = userProfileService.getAvatarBytes(userId, principal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(bytes);
    }
}
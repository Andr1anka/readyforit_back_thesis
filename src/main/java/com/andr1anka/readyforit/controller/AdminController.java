package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.*;
import com.andr1anka.readyforit.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.andr1anka.readyforit.model.InterviewerRequest;
import com.andr1anka.readyforit.repository.InterviewerRequestRepository;
import com.andr1anka.readyforit.service.FileStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final FileStorageService fileStorageService;
    private final InterviewerRequestRepository interviewerRequestRepository;

    // --- Скарги ---
    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintViewDTO>> complaints(
            @RequestParam(required = false, defaultValue = "OPEN") String status) {
        return ResponseEntity.ok(adminService.getComplaints(status));
    }

    @PostMapping("/complaints/{id}/resolve")
    public ResponseEntity<ComplaintViewDTO> resolveComplaint(
            @PathVariable Long id,
            @RequestParam boolean accept,
            @RequestBody(required = false) AdminActionDTO action) {
        String comment = action == null ? null : action.getComment();
        return ResponseEntity.ok(adminService.resolveComplaint(id, accept, comment));
    }

    // --- Користувачі ---
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> users() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<AdminUserDTO> block(@PathVariable Long id, @RequestParam boolean blocked) {
        return ResponseEntity.ok(adminService.setBlocked(id, blocked));
    }

    // --- Заявки інтерв'юерів ---
    @GetMapping("/requests")
    public ResponseEntity<List<AdminRequestDTO>> pendingRequests() {
        return ResponseEntity.ok(adminService.getPendingRequests());
    }

    @PostMapping("/requests/{id}/decide")
    public ResponseEntity<AdminRequestDTO> decideRequest(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestBody(required = false) AdminActionDTO action) {
        String comment = action == null ? null : action.getComment();
        return ResponseEntity.ok(adminService.decideRequest(id, approve, comment));
    }
    @GetMapping("/requests/{requestId}/proofs/{index}")
    public ResponseEntity<byte[]> openProof(
            @PathVariable Long requestId,
            @PathVariable int index
    ) {
        InterviewerRequest request = interviewerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявку не знайдено"));

        if (request.getProofObjectKeys() == null ||
                index < 0 ||
                index >= request.getProofObjectKeys().size()) {
            throw new RuntimeException("Файл не знайдено");
        }

        String objectKey = request.getProofObjectKeys().get(index);

        byte[] bytes = fileStorageService.getDecryptedBytes(
                fileStorageService.getInterviewerProofsBucket(),
                objectKey
        );

        MediaType mediaType = detectMediaType(bytes, objectKey);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(mediaType)
                .body(bytes);
    }

    private MediaType detectMediaType(byte[] bytes, String objectKey) {
        if (bytes == null || bytes.length < 12) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        // PDF: %PDF
        if (bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46) {
            return MediaType.APPLICATION_PDF;
        }

        // PNG
        if ((bytes[0] & 0xFF) == 0x89 &&
                bytes[1] == 0x50 &&
                bytes[2] == 0x4E &&
                bytes[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }

        // JPG / JPEG
        if ((bytes[0] & 0xFF) == 0xFF &&
                (bytes[1] & 0xFF) == 0xD8 &&
                (bytes[2] & 0xFF) == 0xFF) {
            return MediaType.IMAGE_JPEG;
        }

        // GIF
        if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) {
            return MediaType.IMAGE_GIF;
        }

        // WEBP: RIFF....WEBP
        if (bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46 &&
                bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50) {
            return MediaType.parseMediaType("image/webp");
        }

        String lower = objectKey == null ? "" : objectKey.toLowerCase();

        if (lower.endsWith(".txt")) return MediaType.TEXT_PLAIN;
        if (lower.endsWith(".csv")) return MediaType.parseMediaType("text/csv");
        if (lower.endsWith(".json")) return MediaType.APPLICATION_JSON;
        if (lower.endsWith(".xml")) return MediaType.APPLICATION_XML;
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return MediaType.TEXT_HTML;
        if (lower.endsWith(".mp4")) return MediaType.parseMediaType("video/mp4");
        if (lower.endsWith(".webm")) return MediaType.parseMediaType("video/webm");
        if (lower.endsWith(".mp3")) return MediaType.parseMediaType("audio/mpeg");
        if (lower.endsWith(".wav")) return MediaType.parseMediaType("audio/wav");
        if (lower.endsWith(".doc")) return MediaType.parseMediaType("application/msword");
        if (lower.endsWith(".docx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    // --- Верифікація ---
    @GetMapping("/verifications")
    public ResponseEntity<List<AdminUserDTO>> verificationQueue() {
        return ResponseEntity.ok(adminService.getVerificationQueue());
    }

    @PostMapping("/verifications/{userId}/decide")
    public ResponseEntity<AdminUserDTO> decideVerification(
            @PathVariable Long userId,
            @RequestParam boolean approve) {
        return ResponseEntity.ok(adminService.decideVerification(userId, approve));
    }
}

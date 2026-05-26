package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.service.FileEncryptionService;
import com.andr1anka.readyforit.service.FileStorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final Set<String> ALLOWED_DOC_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf"
    );

    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;       // 5 MB
    private static final long MAX_DOCUMENT_SIZE = 15L * 1024 * 1024;    // 15 MB

    private final MinioClient minioClient;
    private final FileEncryptionService encryptionService;

    @Value("${minio.bucket.avatars}")
    private String avatarsBucket;

    @Value("${minio.bucket.kyc}")
    private String kycBucket;

    @Value("${minio.bucket.interviewer-proofs}")
    private String interviewerProofsBucket;

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        validate(file, ALLOWED_IMAGE_MIME, MAX_AVATAR_SIZE);
        String key = "user-" + userId + "/avatar-" + UUID.randomUUID() + extOf(file);

        // Читаємо у пам'ять, щоб передати точний розмір і коректний partSize.
        // Раніше тут йшов file.getInputStream() з partSize=-1 і known size,
        // що в деяких версіях MinIO SDK кидало помилку → 500 при будь-якому збої.
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new BadRequestException("Не вдалося прочитати файл зображення");
        }

        try (InputStream in = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(avatarsBucket)
                            .object(key)
                            .stream(in, bytes.length, -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return key;
        } catch (Exception e) {
            // Найчастіша причина 500 — MinIO недоступний (не запущено на :9000).
            // Перетворюємо технічну помилку на зрозуміле повідомлення для фронта.
            log.error("Avatar upload to MinIO failed (bucket={}, key={}): {}",
                    avatarsBucket, key, e.getMessage(), e);
            throw new BadRequestException(
                    "Сервіс зберігання зображень недоступний. Перевірте, що MinIO запущено.");
        }
    }

    @Override
    public String uploadKycDocument(MultipartFile file, Long userId, String label) {
        validate(file, ALLOWED_DOC_MIME, MAX_DOCUMENT_SIZE);
        return uploadEncrypted(file, kycBucket, "user-" + userId + "/" + label + "-" + UUID.randomUUID() + ".enc");
    }

    @Override
    public String uploadInterviewerProof(MultipartFile file, Long userId) {
        validate(file, ALLOWED_DOC_MIME, MAX_DOCUMENT_SIZE);
        return uploadEncrypted(file, interviewerProofsBucket,
                "user-" + userId + "/proof-" + UUID.randomUUID() + ".enc");
    }

    private String uploadEncrypted(MultipartFile file, String bucket, String key) {
        try {
            byte[] encrypted = encryptionService.encrypt(file.getBytes());
            try (InputStream in = new ByteArrayInputStream(encrypted)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .stream(in, encrypted.length, -1)
                                .contentType("application/octet-stream")
                                .build()
                );
            }
            return key;
        } catch (Exception e) {
            throw new IllegalStateException("Encrypted upload failed", e);
        }
    }

    @Override
    public byte[] getDecryptedBytes(String bucket, String objectKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
            byte[] encrypted = stream.readAllBytes();
            return encryptionService.decrypt(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch " + objectKey, e);
        }
    }

    @Override
    public byte[] getRawBytes(String bucket, String objectKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch " + objectKey, e);
        }
    }

    @Override
    public void delete(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build()
            );
        } catch (Exception e) {
            log.warn("Failed to delete {} from {}: {}", objectKey, bucket, e.getMessage());
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String objectKey, int expirationSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expirationSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public String getAvatarsBucket() { return avatarsBucket; }

    @Override
    public String getKycBucket() { return kycBucket; }

    @Override
    public String getInterviewerProofsBucket() { return interviewerProofsBucket; }

    private void validate(MultipartFile file, Set<String> allowedMimeTypes, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл відсутній");
        }
        if (file.getSize() > maxSize) {
            throw new BadRequestException("Файл занадто великий (макс. " + (maxSize / 1024 / 1024) + " MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Недопустимий тип файлу: " + contentType);
        }
    }

    private String extOf(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null) return "";
        return switch (ct) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}
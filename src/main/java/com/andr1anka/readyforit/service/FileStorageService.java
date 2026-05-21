package com.andr1anka.readyforit.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    /** Завантажити аватар (НЕ шифрований, публічний bucket). Повертає об'єктний ключ. */
    String uploadAvatar(MultipartFile file, Long userId);

    /** Завантажити KYC-документ (шифрований AES-GCM + ServerSideEncryption у MinIO). */
    String uploadKycDocument(MultipartFile file, Long userId, String label);

    /** Завантажити доказ досвіду для заявки на інтерв'юера (шифрований). */
    String uploadInterviewerProof(MultipartFile file, Long userId);

    /** Отримати сирий вміст файлу за ключем у конкретному bucket'і. */
    byte[] getDecryptedBytes(String bucket, String objectKey);

    /** Отримати сирий вміст НЕ зашифрованого файлу (для аватарів). */
    byte[] getRawBytes(String bucket, String objectKey);

    /** Видалити об'єкт. */
    void delete(String bucket, String objectKey);

    /** Згенерувати presigned URL на короткий час (для випадків коли треба віддати фронту напряму). */
    String getPresignedUrl(String bucket, String objectKey, int expirationSeconds);

    String getAvatarsBucket();
    String getKycBucket();
    String getInterviewerProofsBucket();
}
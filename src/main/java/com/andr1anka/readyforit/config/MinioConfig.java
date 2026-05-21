package com.andr1anka.readyforit.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Component
    @RequiredArgsConstructor
    @Slf4j
    static class BucketInitializer {

        private final MinioClient minioClient;

        @Value("${minio.bucket.avatars}")
        private String avatarsBucket;

        @Value("${minio.bucket.kyc}")
        private String kycBucket;

        @Value("${minio.bucket.interviewer-proofs}")
        private String interviewerProofsBucket;

        @PostConstruct
        public void init() {
            ensureBucket(avatarsBucket);
            ensureBucket(kycBucket);
            ensureBucket(interviewerProofsBucket);
        }

        private void ensureBucket(String name) {
            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(name).build()
                );
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
                    log.info("Created MinIO bucket: {}", name);
                } else {
                    log.info("MinIO bucket already exists: {}", name);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to ensure MinIO bucket " + name, e);
            }
        }
    }
}
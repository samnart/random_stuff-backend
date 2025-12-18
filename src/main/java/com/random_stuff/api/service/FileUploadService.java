package com.random_stuff.api.service;

import com.random_stuff.api.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    @Value("${aws.s3.bucket:randomstuff-uploads}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    private S3Client s3Client;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final String LOCAL_UPLOAD_DIR = "uploads";

    @PostConstruct
    public void init() {
        if (s3Enabled && !accessKey.isEmpty() && !secretKey.isEmpty()) {
            s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
            log.info("S3 client initialized for bucket: {}", bucketName);
        } else {
            log.info("S3 disabled, using local storage");
            // Create local upload directory
            try {
                Files.createDirectories(Paths.get(LOCAL_UPLOAD_DIR));
            } catch (IOException e) {
                log.error("Failed to create upload directory", e);
            }
        }
    }

    public UploadResult uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String key = folder + "/" + fileName;

        try {
            if (s3Enabled && s3Client != null) {
                return uploadToS3(file, key);
            } else {
                return uploadToLocal(file, key);
            }
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    public List<UploadResult> uploadImages(List<MultipartFile> files, String folder) {
        return files.stream()
            .map(file -> uploadImage(file, folder))
            .toList();
    }

    public void deleteImage(String fileUrl) {
        try {
            if (s3Enabled && s3Client != null) {
                String key = extractKeyFromUrl(fileUrl);
                deleteFromS3(key);
            } else {
                deleteFromLocal(fileUrl);
            }
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    // ===== S3 OPERATIONS =====

    private UploadResult uploadToS3(MultipartFile file, String key) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.getContentType())
            .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);

        log.info("Uploaded to S3: {}", url);

        return UploadResult.builder()
            .url(url)
            .key(key)
            .fileName(file.getOriginalFilename())
            .size(file.getSize())
            .contentType(file.getContentType())
            .build();
    }

    private void deleteFromS3(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        s3Client.deleteObject(request);
        log.info("Deleted from S3: {}", key);
    }

    // ===== LOCAL STORAGE OPERATIONS =====

    private UploadResult uploadToLocal(MultipartFile file, String key) throws IOException {
        Path filePath = Paths.get(LOCAL_UPLOAD_DIR, key);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        String url = "/uploads/" + key;

        log.info("Uploaded to local: {}", url);

        return UploadResult.builder()
            .url(url)
            .key(key)
            .fileName(file.getOriginalFilename())
            .size(file.getSize())
            .contentType(file.getContentType())
            .build();
    }

    private void deleteFromLocal(String fileUrl) throws IOException {
        String key = fileUrl.replace("/uploads/", "");
        Path filePath = Paths.get(LOCAL_UPLOAD_DIR, key);
        Files.deleteIfExists(filePath);
        log.info("Deleted from local: {}", key);
    }

    // ===== HELPER METHODS =====

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed size (10MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String extractKeyFromUrl(String url) {
        if (url.contains(".amazonaws.com/")) {
            return url.substring(url.indexOf(".amazonaws.com/") + 15);
        }
        return url.replace("/uploads/", "");
    }

    // ===== DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadResult {
        private String url;
        private String key;
        private String fileName;
        private long size;
        private String contentType;
    }
}

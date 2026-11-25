package com.nijakaam.service.impl;

import com.nijakaam.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );

    private final Path uploadLocation = Paths.get("uploads", "profile-photos").toAbsolutePath().normalize();

    @Value("${cloudinary.cloud-name:#{environment.CLOUDINARY_CLOUD_NAME}}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key:#{environment.CLOUDINARY_API_KEY}}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret:#{environment.CLOUDINARY_API_SECRET}}")
    private String cloudinaryApiSecret;

    public FileUploadServiceImpl() {
        try {
            Files.createDirectories(uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage location", e);
        }
    }

    @Override
    public String uploadProfilePhoto(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required and cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must not exceed 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid image format. Allowed formats are JPG, JPEG, PNG, and WEBP");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file extension. Allowed extensions are .jpg, .jpeg, .png, and .webp");
        }

        // Generate safe unique filename linked to user ID
        String filename = "user-" + userId + "-" + UUID.randomUUID() + "." + extension.toLowerCase();

        try {
            Path targetLocation = this.uploadLocation.resolve(filename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Build canonical URL
            try {
                return ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/uploads/profile-photos/")
                        .path(filename)
                        .toUriString();
            } catch (Exception ex) {
                return "http://localhost:8080/api/uploads/profile-photos/" + filename;
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save profile photo: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "jpg";
    }
}

package com.nijakaam.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    String uploadProfilePhoto(MultipartFile file, String userId);

}

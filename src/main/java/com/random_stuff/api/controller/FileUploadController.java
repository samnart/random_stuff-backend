package com.random_stuff.api.controller;

import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.service.FileUploadService;
import com.random_stuff.api.service.FileUploadService.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UploadResult>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "images") String folder) {
        UploadResult result = fileUploadService.uploadImage(file, folder);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", result));
    }

    @PostMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UploadResult>>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "images") String folder) {
        List<UploadResult> results = fileUploadService.uploadImages(files, folder);
        return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", results));
    }

    @PostMapping("/product-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UploadResult>>> uploadProductImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam String productId) {
        String folder = "products/" + productId;
        List<UploadResult> results = fileUploadService.uploadImages(files, folder);
        return ResponseEntity.ok(ApiResponse.success("Product images uploaded successfully", results));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam String url) {
        fileUploadService.deleteImage(url);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }
}

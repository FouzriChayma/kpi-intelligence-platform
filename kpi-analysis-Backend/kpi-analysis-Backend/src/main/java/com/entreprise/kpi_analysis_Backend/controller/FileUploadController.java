package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.dto.FileUploadResponse;
import com.entreprise.kpi_analysis_Backend.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * REST Controller for file upload operations (Excel/CSV import)
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private final FileUploadService fileUploadService;
    
    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }
    
    /**
     * Upload and process Excel/CSV file for KPI import
     * 
     * @param file The uploaded file (Excel or CSV)
     * @param periodStart Start date of the KPI period (format: yyyy-MM-dd)
     * @param periodEnd End date of the KPI period (format: yyyy-MM-dd)
     * @return FileUploadResponse with processing results
     */
    @PostMapping("/kpi-file")
    public ResponseEntity<FileUploadResponse> uploadKPIFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "periodStart", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(value = "periodEnd", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {
        
        logger.info("Received file upload request: {} ({} bytes)", 
            file.getOriginalFilename(), file.getSize());
        
        // Validate file
        if (file.isEmpty()) {
            FileUploadResponse response = new FileUploadResponse(false, "Le fichier est vide");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Set default period to current month if not provided
        if (periodStart == null || periodEnd == null) {
            LocalDate now = LocalDate.now();
            periodStart = now.withDayOfMonth(1);
            periodEnd = now.withDayOfMonth(now.lengthOfMonth());
            logger.info("Using default period: {} to {}", periodStart, periodEnd);
        }
        
        // Validate period
        if (periodStart.isAfter(periodEnd)) {
            FileUploadResponse response = new FileUploadResponse(false, 
                "La date de début doit être antérieure à la date de fin");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            FileUploadResponse response = fileUploadService.processFile(file, periodStart, periodEnd);
            
            if (response.isSuccess()) {
                logger.info("File processed successfully: {}", response.getMessage());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("File processing failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing uploaded file", e);
            FileUploadResponse response = new FileUploadResponse(false, 
                "Erreur lors du traitement du fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}


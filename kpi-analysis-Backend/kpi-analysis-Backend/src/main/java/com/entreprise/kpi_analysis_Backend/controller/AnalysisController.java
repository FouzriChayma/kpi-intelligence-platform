package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.service.AIAnalysisService;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for AI Analysis operations
 */
@RestController
@RequestMapping("/api/analysis")
@Validated
public class AnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    private final AIAnalysisService aiAnalysisService;
    
    @Autowired
    public AnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }
    
    /**
     * Analyze employee performance
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Map<String, String>> analyzeEmployee(
            @PathVariable @Min(value = 1, message = "Employee ID must be greater than 0") Long employeeId) {
        logger.debug("Analyzing employee performance for ID: {}", employeeId);
        String analysis = aiAnalysisService.analyzeEmployeePerformance(employeeId);
        Map<String, String> response = new HashMap<>();
        response.put("analysis", analysis);
        response.put("employeeId", employeeId.toString());
        logger.info("Analysis completed for employee: {}", employeeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get AI recommendations for an employee
     */
    @GetMapping("/employee/{employeeId}/recommendations")
    public ResponseEntity<Map<String, String>> getRecommendations(
            @PathVariable @Min(value = 1, message = "Employee ID must be greater than 0") Long employeeId) {
        logger.debug("Generating recommendations for employee ID: {}", employeeId);
        String recommendations = aiAnalysisService.generateRecommendations(employeeId);
        Map<String, String> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("employeeId", employeeId.toString());
        logger.info("Recommendations generated for employee: {}", employeeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Trigger AI analysis update for a KPI
     */
    @PostMapping("/kpi/{kpiId}/analyze")
    public ResponseEntity<Map<String, String>> analyzeKPI(
            @PathVariable @Min(value = 1, message = "KPI ID must be greater than 0") Long kpiId) {
        logger.debug("Triggering AI analysis for KPI ID: {}", kpiId);
        aiAnalysisService.updateKPIWithAnalysis(kpiId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "KPI analysis updated successfully");
        response.put("kpiId", kpiId.toString());
        logger.info("AI analysis completed for KPI: {}", kpiId);
        return ResponseEntity.ok(response);
    }
}


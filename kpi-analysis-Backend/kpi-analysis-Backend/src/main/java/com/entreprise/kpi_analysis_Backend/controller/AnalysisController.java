package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.service.AIAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {
    
    private final AIAnalysisService aiAnalysisService;
    
    @Autowired
    public AnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }
    
    /**
     * Analyze employee performance
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Map<String, String>> analyzeEmployee(@PathVariable Long employeeId) {
        String analysis = aiAnalysisService.analyzeEmployeePerformance(employeeId);
        Map<String, String> response = new HashMap<>();
        response.put("analysis", analysis);
        response.put("employeeId", employeeId.toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get AI recommendations for an employee
     */
    @GetMapping("/employee/{employeeId}/recommendations")
    public ResponseEntity<Map<String, String>> getRecommendations(@PathVariable Long employeeId) {
        String recommendations = aiAnalysisService.generateRecommendations(employeeId);
        Map<String, String> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("employeeId", employeeId.toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Trigger AI analysis update for a KPI
     */
    @PostMapping("/kpi/{kpiId}/analyze")
    public ResponseEntity<Map<String, String>> analyzeKPI(@PathVariable Long kpiId) {
        aiAnalysisService.updateKPIWithAnalysis(kpiId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "KPI analysis updated successfully");
        response.put("kpiId", kpiId.toString());
        return ResponseEntity.ok(response);
    }
}


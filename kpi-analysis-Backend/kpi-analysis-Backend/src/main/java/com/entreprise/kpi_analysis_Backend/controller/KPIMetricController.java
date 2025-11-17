package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.dto.KPIMetricDTO;
import com.entreprise.kpi_analysis_Backend.service.KPIMetricService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for KPI Metric operations
 */
@RestController
@RequestMapping("/api/kpi-metrics")
@Validated
public class KPIMetricController {
    
    private static final Logger logger = LoggerFactory.getLogger(KPIMetricController.class);
    private final KPIMetricService kpiMetricService;
    
    @Autowired
    public KPIMetricController(KPIMetricService kpiMetricService) {
        this.kpiMetricService = kpiMetricService;
    }
    
    @GetMapping
    public ResponseEntity<List<KPIMetricDTO>> getAllKPIMetrics() {
        logger.debug("Fetching all KPI metrics");
        List<KPIMetricDTO> metrics = kpiMetricService.getAllKPIMetrics();
        logger.info("Retrieved {} KPI metrics", metrics.size());
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<KPIMetricDTO> getKPIMetricById(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id) {
        logger.debug("Fetching KPI metric with ID: {}", id);
        KPIMetricDTO metric = kpiMetricService.getKPIMetricById(id);
        logger.info("Retrieved KPI metric: {}", id);
        return ResponseEntity.ok(metric);
    }
    
    @GetMapping("/kpi/{kpiId}")
    public ResponseEntity<List<KPIMetricDTO>> getKPIMetricsByKpiId(
            @PathVariable @Min(value = 1, message = "KPI ID must be greater than 0") Long kpiId) {
        logger.debug("Fetching KPI metrics for KPI ID: {}", kpiId);
        List<KPIMetricDTO> metrics = kpiMetricService.getKPIMetricsByKpiId(kpiId);
        logger.info("Retrieved {} KPI metrics for KPI: {}", metrics.size(), kpiId);
        return ResponseEntity.ok(metrics);
    }
    
    @PostMapping
    public ResponseEntity<KPIMetricDTO> createKPIMetric(@Valid @RequestBody KPIMetricDTO metricDTO) {
        logger.debug("Creating new KPI metric for KPI ID: {}", metricDTO.getKpiId());
        KPIMetricDTO createdMetric = kpiMetricService.createKPIMetric(metricDTO);
        logger.info("Created KPI metric with ID: {}", createdMetric.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMetric);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<KPIMetricDTO> updateKPIMetric(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id, 
            @Valid @RequestBody KPIMetricDTO metricDTO) {
        logger.debug("Updating KPI metric with ID: {}", id);
        KPIMetricDTO updatedMetric = kpiMetricService.updateKPIMetric(id, metricDTO);
        logger.info("Updated KPI metric with ID: {}", id);
        return ResponseEntity.ok(updatedMetric);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKPIMetric(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id) {
        logger.debug("Deleting KPI metric with ID: {}", id);
        kpiMetricService.deleteKPIMetric(id);
        logger.info("Deleted KPI metric with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}


package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.dto.KPIMetricDTO;
import com.entreprise.kpi_analysis_Backend.service.KPIMetricService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kpi-metrics")
@CrossOrigin(origins = "*")
public class KPIMetricController {
    
    private final KPIMetricService kpiMetricService;
    
    @Autowired
    public KPIMetricController(KPIMetricService kpiMetricService) {
        this.kpiMetricService = kpiMetricService;
    }
    
    @GetMapping
    public ResponseEntity<List<KPIMetricDTO>> getAllKPIMetrics() {
        return ResponseEntity.ok(kpiMetricService.getAllKPIMetrics());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<KPIMetricDTO> getKPIMetricById(@PathVariable Long id) {
        return ResponseEntity.ok(kpiMetricService.getKPIMetricById(id));
    }
    
    @GetMapping("/kpi/{kpiId}")
    public ResponseEntity<List<KPIMetricDTO>> getKPIMetricsByKpiId(@PathVariable Long kpiId) {
        return ResponseEntity.ok(kpiMetricService.getKPIMetricsByKpiId(kpiId));
    }
    
    @PostMapping
    public ResponseEntity<KPIMetricDTO> createKPIMetric(@Valid @RequestBody KPIMetricDTO metricDTO) {
        KPIMetricDTO createdMetric = kpiMetricService.createKPIMetric(metricDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMetric);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<KPIMetricDTO> updateKPIMetric(@PathVariable Long id, 
                                                         @Valid @RequestBody KPIMetricDTO metricDTO) {
        KPIMetricDTO updatedMetric = kpiMetricService.updateKPIMetric(id, metricDTO);
        return ResponseEntity.ok(updatedMetric);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKPIMetric(@PathVariable Long id) {
        kpiMetricService.deleteKPIMetric(id);
        return ResponseEntity.noContent().build();
    }
}


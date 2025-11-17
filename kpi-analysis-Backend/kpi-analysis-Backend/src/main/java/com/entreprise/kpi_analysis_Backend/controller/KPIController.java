package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.dto.KPIDTO;
import com.entreprise.kpi_analysis_Backend.service.KPIService;
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
 * REST Controller for KPI operations
 */
@RestController
@RequestMapping("/api/kpis")
@Validated
public class KPIController {
    
    private static final Logger logger = LoggerFactory.getLogger(KPIController.class);
    private final KPIService kpiService;
    
    @Autowired
    public KPIController(KPIService kpiService) {
        this.kpiService = kpiService;
    }
    
    @GetMapping
    public ResponseEntity<List<KPIDTO>> getAllKPIs() {
        logger.debug("Fetching all KPIs");
        List<KPIDTO> kpis = kpiService.getAllKPIs();
        logger.info("Retrieved {} KPIs", kpis.size());
        return ResponseEntity.ok(kpis);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<KPIDTO> getKPIById(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id) {
        logger.debug("Fetching KPI with ID: {}", id);
        KPIDTO kpi = kpiService.getKPIById(id);
        logger.info("Retrieved KPI: {}", id);
        return ResponseEntity.ok(kpi);
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<KPIDTO>> getKPIsByEmployeeId(
            @PathVariable @Min(value = 1, message = "Employee ID must be greater than 0") Long employeeId) {
        logger.debug("Fetching KPIs for employee ID: {}", employeeId);
        List<KPIDTO> kpis = kpiService.getKPIsByEmployeeId(employeeId);
        logger.info("Retrieved {} KPIs for employee: {}", kpis.size(), employeeId);
        return ResponseEntity.ok(kpis);
    }
    
    @PostMapping
    public ResponseEntity<KPIDTO> createKPI(@Valid @RequestBody KPIDTO kpiDTO) {
        logger.debug("Creating new KPI for employee ID: {}", kpiDTO.getEmployeeId());
        KPIDTO createdKPI = kpiService.createKPI(kpiDTO);
        logger.info("Created KPI with ID: {}", createdKPI.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdKPI);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<KPIDTO> updateKPI(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id, 
            @Valid @RequestBody KPIDTO kpiDTO) {
        logger.debug("Updating KPI with ID: {}", id);
        KPIDTO updatedKPI = kpiService.updateKPI(id, kpiDTO);
        logger.info("Updated KPI with ID: {}", id);
        return ResponseEntity.ok(updatedKPI);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKPI(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id) {
        logger.debug("Deleting KPI with ID: {}", id);
        kpiService.deleteKPI(id);
        logger.info("Deleted KPI with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}

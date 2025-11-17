package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.dto.KPIDTO;
import com.entreprise.kpi_analysis_Backend.service.KPIService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kpis")
@CrossOrigin(origins = "*")
public class KPIController {
    
    private final KPIService kpiService;
    
    @Autowired
    public KPIController(KPIService kpiService) {
        this.kpiService = kpiService;
    }
    
    @GetMapping
    public ResponseEntity<List<KPIDTO>> getAllKPIs() {
        return ResponseEntity.ok(kpiService.getAllKPIs());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<KPIDTO> getKPIById(@PathVariable Long id) {
        return ResponseEntity.ok(kpiService.getKPIById(id));
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<KPIDTO>> getKPIsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(kpiService.getKPIsByEmployeeId(employeeId));
    }
    
    @PostMapping
    public ResponseEntity<KPIDTO> createKPI(@Valid @RequestBody KPIDTO kpiDTO) {
        KPIDTO createdKPI = kpiService.createKPI(kpiDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdKPI);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<KPIDTO> updateKPI(@PathVariable Long id, 
                                            @Valid @RequestBody KPIDTO kpiDTO) {
        KPIDTO updatedKPI = kpiService.updateKPI(id, kpiDTO);
        return ResponseEntity.ok(updatedKPI);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKPI(@PathVariable Long id) {
        kpiService.deleteKPI(id);
        return ResponseEntity.noContent().build();
    }
}

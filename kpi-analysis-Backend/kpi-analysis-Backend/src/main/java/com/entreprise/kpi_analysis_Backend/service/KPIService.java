package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.dto.KPIDTO;
import com.entreprise.kpi_analysis_Backend.entity.KPI;
import com.entreprise.kpi_analysis_Backend.exception.ResourceNotFoundException;
import com.entreprise.kpi_analysis_Backend.repository.KPIRepository;
import com.entreprise.kpi_analysis_Backend.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for KPI business logic
 */
@Service
@Transactional
public class KPIService {
    
    private static final Logger logger = LoggerFactory.getLogger(KPIService.class);
    private final KPIRepository kpiRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public KPIService(KPIRepository kpiRepository, EmployeeRepository employeeRepository) {
        this.kpiRepository = kpiRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Transactional(readOnly = true)
    public List<KPIDTO> getAllKPIs() {
        logger.debug("Fetching all KPIs from repository");
        return kpiRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public KPIDTO getKPIById(Long id) {
        logger.debug("Fetching KPI with ID: {}", id);
        KPI kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI", id));
        return convertToDTO(kpi);
    }
    
    @Transactional(readOnly = true)
    public List<KPIDTO> getKPIsByEmployeeId(Long employeeId) {
        logger.debug("Fetching KPIs for employee ID: {}", employeeId);
        return kpiRepository.findByEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public KPIDTO createKPI(KPIDTO kpiDTO) {
        logger.debug("Creating new KPI for employee ID: {}", kpiDTO.getEmployeeId());
        // Verify employee exists
        if (!employeeRepository.existsById(kpiDTO.getEmployeeId())) {
            throw new ResourceNotFoundException("Employee", kpiDTO.getEmployeeId());
        }
        
        KPI kpi = convertToEntity(kpiDTO);
        KPI savedKPI = kpiRepository.save(kpi);
        logger.info("KPI created successfully with ID: {}", savedKPI.getId());
        return convertToDTO(savedKPI);
    }
    
    public KPIDTO updateKPI(Long id, KPIDTO kpiDTO) {
        logger.debug("Updating KPI with ID: {}", id);
        KPI kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI", id));
        
        kpi.setPeriodStart(kpiDTO.getPeriodStart());
        kpi.setPeriodEnd(kpiDTO.getPeriodEnd());
        kpi.setOverallScore(kpiDTO.getOverallScore());
        kpi.setAiAnalysis(kpiDTO.getAiAnalysis());
        kpi.setAiRecommendations(kpiDTO.getAiRecommendations());
        
        KPI updatedKPI = kpiRepository.save(kpi);
        logger.info("KPI updated successfully with ID: {}", id);
        return convertToDTO(updatedKPI);
    }
    
    public void deleteKPI(Long id) {
        logger.debug("Deleting KPI with ID: {}", id);
        if (!kpiRepository.existsById(id)) {
            throw new ResourceNotFoundException("KPI", id);
        }
        kpiRepository.deleteById(id);
        logger.info("KPI deleted successfully with ID: {}", id);
    }
    
    // Helper methods for conversion
    private KPIDTO convertToDTO(KPI kpi) {
        KPIDTO dto = new KPIDTO();
        dto.setId(kpi.getId());
        dto.setEmployeeId(kpi.getEmployee().getId());
        dto.setPeriodStart(kpi.getPeriodStart());
        dto.setPeriodEnd(kpi.getPeriodEnd());
        dto.setOverallScore(kpi.getOverallScore());
        dto.setAiAnalysis(kpi.getAiAnalysis());
        dto.setAiRecommendations(kpi.getAiRecommendations());
        return dto;
    }
    
    private KPI convertToEntity(KPIDTO dto) {
        KPI kpi = new KPI();
        kpi.setId(dto.getId());
        // Set employee reference
        kpi.setEmployee(employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", dto.getEmployeeId())));
        kpi.setPeriodStart(dto.getPeriodStart());
        kpi.setPeriodEnd(dto.getPeriodEnd());
        kpi.setOverallScore(dto.getOverallScore());
        kpi.setAiAnalysis(dto.getAiAnalysis());
        kpi.setAiRecommendations(dto.getAiRecommendations());
        return kpi;
    }
}

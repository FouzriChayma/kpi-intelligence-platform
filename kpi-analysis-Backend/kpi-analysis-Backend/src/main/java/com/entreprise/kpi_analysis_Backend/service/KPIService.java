package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.dto.KPIDTO;
import com.entreprise.kpi_analysis_Backend.entity.KPI;
import com.entreprise.kpi_analysis_Backend.exception.ResourceNotFoundException;
import com.entreprise.kpi_analysis_Backend.repository.KPIRepository;
import com.entreprise.kpi_analysis_Backend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KPIService {
    
    private final KPIRepository kpiRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public KPIService(KPIRepository kpiRepository, EmployeeRepository employeeRepository) {
        this.kpiRepository = kpiRepository;
        this.employeeRepository = employeeRepository;
    }
    
    public List<KPIDTO> getAllKPIs() {
        return kpiRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public KPIDTO getKPIById(Long id) {
        KPI kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI", id));
        return convertToDTO(kpi);
    }
    
    public List<KPIDTO> getKPIsByEmployeeId(Long employeeId) {
        return kpiRepository.findByEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public KPIDTO createKPI(KPIDTO kpiDTO) {
        // Verify employee exists
        if (!employeeRepository.existsById(kpiDTO.getEmployeeId())) {
            throw new ResourceNotFoundException("Employee", kpiDTO.getEmployeeId());
        }
        
        KPI kpi = convertToEntity(kpiDTO);
        KPI savedKPI = kpiRepository.save(kpi);
        return convertToDTO(savedKPI);
    }
    
    public KPIDTO updateKPI(Long id, KPIDTO kpiDTO) {
        KPI kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI", id));
        
        kpi.setPeriodStart(kpiDTO.getPeriodStart());
        kpi.setPeriodEnd(kpiDTO.getPeriodEnd());
        kpi.setOverallScore(kpiDTO.getOverallScore());
        kpi.setAiAnalysis(kpiDTO.getAiAnalysis());
        kpi.setAiRecommendations(kpiDTO.getAiRecommendations());
        
        KPI updatedKPI = kpiRepository.save(kpi);
        return convertToDTO(updatedKPI);
    }
    
    public void deleteKPI(Long id) {
        if (!kpiRepository.existsById(id)) {
            throw new ResourceNotFoundException("KPI", id);
        }
        kpiRepository.deleteById(id);
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

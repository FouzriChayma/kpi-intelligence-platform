package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.dto.KPIMetricDTO;
import com.entreprise.kpi_analysis_Backend.entity.KPIMetric;
import com.entreprise.kpi_analysis_Backend.exception.ResourceNotFoundException;
import com.entreprise.kpi_analysis_Backend.repository.KPIMetricRepository;
import com.entreprise.kpi_analysis_Backend.repository.KPIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KPIMetricService {
    
    private final KPIMetricRepository kpiMetricRepository;
    private final KPIRepository kpiRepository;
    
    @Autowired
    public KPIMetricService(KPIMetricRepository kpiMetricRepository, KPIRepository kpiRepository) {
        this.kpiMetricRepository = kpiMetricRepository;
        this.kpiRepository = kpiRepository;
    }
    
    public List<KPIMetricDTO> getAllKPIMetrics() {
        return kpiMetricRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public KPIMetricDTO getKPIMetricById(Long id) {
        KPIMetric metric = kpiMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPIMetric", id));
        return convertToDTO(metric);
    }
    
    public List<KPIMetricDTO> getKPIMetricsByKpiId(Long kpiId) {
        return kpiMetricRepository.findByKpiId(kpiId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public KPIMetricDTO createKPIMetric(KPIMetricDTO metricDTO) {
        // Verify KPI exists
        if (!kpiRepository.existsById(metricDTO.getKpiId())) {
            throw new ResourceNotFoundException("KPI", metricDTO.getKpiId());
        }
        
        KPIMetric metric = convertToEntity(metricDTO);
        KPIMetric savedMetric = kpiMetricRepository.save(metric);
        return convertToDTO(savedMetric);
    }
    
    public KPIMetricDTO updateKPIMetric(Long id, KPIMetricDTO metricDTO) {
        KPIMetric metric = kpiMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPIMetric", id));
        
        metric.setMetricType(metricDTO.getMetricType());
        metric.setValue(metricDTO.getValue());
        metric.setTargetValue(metricDTO.getTargetValue());
        metric.setUnit(metricDTO.getUnit());
        
        KPIMetric updatedMetric = kpiMetricRepository.save(metric);
        return convertToDTO(updatedMetric);
    }
    
    public void deleteKPIMetric(Long id) {
        if (!kpiMetricRepository.existsById(id)) {
            throw new ResourceNotFoundException("KPIMetric", id);
        }
        kpiMetricRepository.deleteById(id);
    }
    
    // Helper methods for conversion
    private KPIMetricDTO convertToDTO(KPIMetric metric) {
        KPIMetricDTO dto = new KPIMetricDTO();
        dto.setId(metric.getId());
        dto.setKpiId(metric.getKpi().getId());
        dto.setMetricType(metric.getMetricType());
        dto.setValue(metric.getValue());
        dto.setTargetValue(metric.getTargetValue());
        dto.setUnit(metric.getUnit());
        return dto;
    }
    
    private KPIMetric convertToEntity(KPIMetricDTO dto) {
        KPIMetric metric = new KPIMetric();
        metric.setId(dto.getId());
        // Set KPI reference
        metric.setKpi(kpiRepository.findById(dto.getKpiId())
                .orElseThrow(() -> new ResourceNotFoundException("KPI", dto.getKpiId())));
        metric.setMetricType(dto.getMetricType());
        metric.setValue(dto.getValue());
        metric.setTargetValue(dto.getTargetValue());
        metric.setUnit(dto.getUnit());
        return metric;
    }
}


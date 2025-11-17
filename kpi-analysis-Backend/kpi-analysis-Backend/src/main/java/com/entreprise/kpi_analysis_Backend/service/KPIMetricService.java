package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.dto.KPIMetricDTO;
import com.entreprise.kpi_analysis_Backend.entity.KPIMetric;
import com.entreprise.kpi_analysis_Backend.exception.ResourceNotFoundException;
import com.entreprise.kpi_analysis_Backend.repository.KPIMetricRepository;
import com.entreprise.kpi_analysis_Backend.repository.KPIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for KPI Metric business logic
 */
@Service
@Transactional
public class KPIMetricService {
    
    private static final Logger logger = LoggerFactory.getLogger(KPIMetricService.class);
    private final KPIMetricRepository kpiMetricRepository;
    private final KPIRepository kpiRepository;
    
    @Autowired
    public KPIMetricService(KPIMetricRepository kpiMetricRepository, KPIRepository kpiRepository) {
        this.kpiMetricRepository = kpiMetricRepository;
        this.kpiRepository = kpiRepository;
    }
    
    @Transactional(readOnly = true)
    public List<KPIMetricDTO> getAllKPIMetrics() {
        logger.debug("Fetching all KPI metrics from repository");
        return kpiMetricRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public KPIMetricDTO getKPIMetricById(Long id) {
        logger.debug("Fetching KPI metric with ID: {}", id);
        KPIMetric metric = kpiMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPIMetric", id));
        return convertToDTO(metric);
    }
    
    @Transactional(readOnly = true)
    public List<KPIMetricDTO> getKPIMetricsByKpiId(Long kpiId) {
        logger.debug("Fetching KPI metrics for KPI ID: {}", kpiId);
        return kpiMetricRepository.findByKpiId(kpiId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public KPIMetricDTO createKPIMetric(KPIMetricDTO metricDTO) {
        logger.debug("Creating new KPI metric for KPI ID: {}", metricDTO.getKpiId());
        // Verify KPI exists
        if (!kpiRepository.existsById(metricDTO.getKpiId())) {
            throw new ResourceNotFoundException("KPI", metricDTO.getKpiId());
        }
        
        KPIMetric metric = convertToEntity(metricDTO);
        KPIMetric savedMetric = kpiMetricRepository.save(metric);
        logger.info("KPI metric created successfully with ID: {}", savedMetric.getId());
        return convertToDTO(savedMetric);
    }
    
    public KPIMetricDTO updateKPIMetric(Long id, KPIMetricDTO metricDTO) {
        logger.debug("Updating KPI metric with ID: {}", id);
        KPIMetric metric = kpiMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPIMetric", id));
        
        metric.setMetricType(metricDTO.getMetricType());
        metric.setValue(metricDTO.getValue());
        metric.setTargetValue(metricDTO.getTargetValue());
        metric.setUnit(metricDTO.getUnit());
        
        KPIMetric updatedMetric = kpiMetricRepository.save(metric);
        logger.info("KPI metric updated successfully with ID: {}", id);
        return convertToDTO(updatedMetric);
    }
    
    public void deleteKPIMetric(Long id) {
        logger.debug("Deleting KPI metric with ID: {}", id);
        if (!kpiMetricRepository.existsById(id)) {
            throw new ResourceNotFoundException("KPIMetric", id);
        }
        kpiMetricRepository.deleteById(id);
        logger.info("KPI metric deleted successfully with ID: {}", id);
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


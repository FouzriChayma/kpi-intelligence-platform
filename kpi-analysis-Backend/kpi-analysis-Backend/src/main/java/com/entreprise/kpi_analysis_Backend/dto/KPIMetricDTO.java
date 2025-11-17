package com.entreprise.kpi_analysis_Backend.dto;

import com.entreprise.kpi_analysis_Backend.entity.KPIMetric;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KPIMetricDTO {

    private Long id;

    private Long kpiId;

    @NotNull(message = "Metric type is required")
    private KPIMetric.MetricType metricType;

    @NotNull(message = "Value is required")
    private Double value;

    private Double targetValue;

    private String unit;

    // Explicit getters and setters (Lombok should generate these, but adding for compatibility)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKpiId() {
        return kpiId;
    }

    public void setKpiId(Long kpiId) {
        this.kpiId = kpiId;
    }

    public KPIMetric.MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(KPIMetric.MetricType metricType) {
        this.metricType = metricType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Double targetValue) {
        this.targetValue = targetValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
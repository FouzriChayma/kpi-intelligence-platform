package com.entreprise.kpi_analysis_Backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KPIMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KPI kpi;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MetricType metricType;
    
    @Column(name = "metric_value", nullable = false)
    private Double value;
    
    @Column(name = "target_value")
    private Double targetValue;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum MetricType {
        ATTENDANCE,      // Assiduité
        VELOCITY,        // Vélocité
        QUALITY,         // Qualité
        PRODUCTIVITY,    // Productivité
        EFFICIENCY       // Efficacité
    }
    
    // Explicit getters and setters (Lombok should generate these, but adding for compatibility)
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public KPI getKpi() {
        return kpi;
    }
    
    public void setKpi(KPI kpi) {
        this.kpi = kpi;
    }
    
    public MetricType getMetricType() {
        return metricType;
    }
    
    public void setMetricType(MetricType metricType) {
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

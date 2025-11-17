package com.entreprise.kpi_analysis_Backend.repository;

import com.entreprise.kpi_analysis_Backend.entity.KPIMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KPIMetricRepository extends JpaRepository<KPIMetric, Long> {
    List<KPIMetric> findByKpiId(Long kpiId);
}

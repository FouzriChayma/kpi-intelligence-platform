package com.entreprise.kpi_analysis_Backend.repository;

import com.entreprise.kpi_analysis_Backend.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {
    List<KPI> findByEmployeeId(Long employeeId);
    List<KPI> findByEmployeeIdAndPeriodStartBetween(Long employeeId, LocalDate start, LocalDate end);
}

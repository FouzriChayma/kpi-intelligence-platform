package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.entity.KPI;
import com.entreprise.kpi_analysis_Backend.entity.KPIMetric;
import com.entreprise.kpi_analysis_Backend.repository.KPIRepository;
import com.entreprise.kpi_analysis_Backend.repository.KPIMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIAnalysisService {
    
    private final KPIRepository kpiRepository;
    private final KPIMetricRepository kpiMetricRepository;
    
    @Autowired
    public AIAnalysisService(KPIRepository kpiRepository, KPIMetricRepository kpiMetricRepository) {
        this.kpiRepository = kpiRepository;
        this.kpiMetricRepository = kpiMetricRepository;
    }
    
    /**
     * Analyze employee performance and generate AI analysis
     */
    public String analyzeEmployeePerformance(Long employeeId) {
        List<KPI> kpis = kpiRepository.findByEmployeeId(employeeId);
        
        if (kpis.isEmpty()) {
            return "Aucune donnée KPI disponible pour cet employé.";
        }
        
        // Get all metrics for the employee's KPIs
        List<KPIMetric> allMetrics = kpis.stream()
                .flatMap(kpi -> kpiMetricRepository.findByKpiId(kpi.getId()).stream())
                .collect(Collectors.toList());
        
        if (allMetrics.isEmpty()) {
            return "Aucune métrique disponible pour l'analyse.";
        }
        
        // Calculate average scores by metric type
        Map<KPIMetric.MetricType, Double> averageScores = allMetrics.stream()
                .collect(Collectors.groupingBy(
                        KPIMetric::getMetricType,
                        Collectors.averagingDouble(metric -> 
                            metric.getTargetValue() != null && metric.getTargetValue() > 0
                                ? (metric.getValue() / metric.getTargetValue()) * 100
                                : metric.getValue()
                        )
                ));
        
        // Generate analysis
        StringBuilder analysis = new StringBuilder();
        analysis.append("Analyse de performance pour l'employé ID: ").append(employeeId).append("\n\n");
        
        // Analyze each metric type
        for (Map.Entry<KPIMetric.MetricType, Double> entry : averageScores.entrySet()) {
            KPIMetric.MetricType type = entry.getKey();
            Double score = entry.getValue();
            
            analysis.append(getMetricTypeLabel(type)).append(": ");
            analysis.append(String.format("%.2f%%", score));
            
            if (score >= 90) {
                analysis.append(" - Excellent niveau de performance.");
            } else if (score >= 75) {
                analysis.append(" - Bon niveau de performance.");
            } else if (score >= 60) {
                analysis.append(" - Performance acceptable, mais peut être améliorée.");
            } else {
                analysis.append(" - Performance en dessous des attentes, nécessite une attention.");
            }
            analysis.append("\n");
        }
        
        // Overall assessment
        double overallAverage = averageScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        analysis.append("\nScore global moyen: ").append(String.format("%.2f%%", overallAverage));
        
        if (overallAverage >= 85) {
            analysis.append("\n\nÉvaluation globale: Performance exceptionnelle. L'employé dépasse les attentes dans tous les domaines.");
        } else if (overallAverage >= 70) {
            analysis.append("\n\nÉvaluation globale: Performance solide. L'employé répond aux attentes avec quelques points forts.");
        } else if (overallAverage >= 55) {
            analysis.append("\n\nÉvaluation globale: Performance moyenne. Des améliorations sont nécessaires dans certains domaines.");
        } else {
            analysis.append("\n\nÉvaluation globale: Performance nécessitant une attention immédiate. Un plan d'amélioration est recommandé.");
        }
        
        return analysis.toString();
    }
    
    /**
     * Generate AI recommendations based on KPI analysis
     */
    public String generateRecommendations(Long employeeId) {
        List<KPI> kpis = kpiRepository.findByEmployeeId(employeeId);
        
        if (kpis.isEmpty()) {
            return "Aucune recommandation disponible. Aucune donnée KPI trouvée.";
        }
        
        List<KPIMetric> allMetrics = kpis.stream()
                .flatMap(kpi -> kpiMetricRepository.findByKpiId(kpi.getId()).stream())
                .collect(Collectors.toList());
        
        if (allMetrics.isEmpty()) {
            return "Aucune recommandation disponible. Aucune métrique trouvée.";
        }
        
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("Recommandations pour l'employé ID: ").append(employeeId).append("\n\n");
        
        // Group metrics by type and analyze
        Map<KPIMetric.MetricType, List<KPIMetric>> metricsByType = allMetrics.stream()
                .collect(Collectors.groupingBy(KPIMetric::getMetricType));
        
        for (Map.Entry<KPIMetric.MetricType, List<KPIMetric>> entry : metricsByType.entrySet()) {
            KPIMetric.MetricType type = entry.getKey();
            List<KPIMetric> metrics = entry.getValue();
            
            double averageScore = metrics.stream()
                    .mapToDouble(metric -> {
                        if (metric.getTargetValue() != null && metric.getTargetValue() > 0) {
                            return (metric.getValue() / metric.getTargetValue()) * 100;
                        }
                        return metric.getValue();
                    })
                    .average()
                    .orElse(0.0);
            
            if (averageScore < 70) {
                recommendations.append("• ").append(getMetricTypeLabel(type))
                        .append(" (").append(String.format("%.1f%%", averageScore))
                        .append("): ").append(getRecommendationForMetricType(type))
                        .append("\n");
            }
        }
        
        // General recommendations
        double overallAverage = allMetrics.stream()
                .mapToDouble(metric -> {
                    if (metric.getTargetValue() != null && metric.getTargetValue() > 0) {
                        return (metric.getValue() / metric.getTargetValue()) * 100;
                    }
                    return metric.getValue();
                })
                .average()
                .orElse(0.0);
        
        if (overallAverage < 60) {
            recommendations.append("\nRecommandations générales:\n");
            recommendations.append("• Organiser une réunion de suivi pour discuter des objectifs et des défis.\n");
            recommendations.append("• Mettre en place un plan d'amélioration personnalisé.\n");
            recommendations.append("• Fournir des ressources de formation supplémentaires si nécessaire.\n");
        }
        
        if (recommendations.length() == recommendations.toString().indexOf("\n\n") + 2) {
            recommendations.append("Aucune recommandation spécifique. Les performances sont globalement satisfaisantes.");
        }
        
        return recommendations.toString();
    }
    
    /**
     * Auto-update KPI with AI analysis and recommendations
     */
    public void updateKPIWithAnalysis(Long kpiId) {
        KPI kpi = kpiRepository.findById(kpiId)
                .orElseThrow(() -> new RuntimeException("KPI not found: " + kpiId));
        
        Long employeeId = kpi.getEmployee().getId();
        
        String analysis = analyzeEmployeePerformance(employeeId);
        String recommendations = generateRecommendations(employeeId);
        
        kpi.setAiAnalysis(analysis);
        kpi.setAiRecommendations(recommendations);
        
        // Calculate overall score based on metrics
        List<KPIMetric> metrics = kpiMetricRepository.findByKpiId(kpiId);
        if (!metrics.isEmpty()) {
            double overallScore = metrics.stream()
                    .mapToDouble(metric -> {
                        if (metric.getTargetValue() != null && metric.getTargetValue() > 0) {
                            return (metric.getValue() / metric.getTargetValue()) * 100;
                        }
                        return metric.getValue();
                    })
                    .average()
                    .orElse(0.0);
            kpi.setOverallScore(overallScore);
        }
        
        kpiRepository.save(kpi);
    }
    
    // Helper methods
    private String getMetricTypeLabel(KPIMetric.MetricType type) {
        return switch (type) {
            case ATTENDANCE -> "Assiduité";
            case VELOCITY -> "Vélocité";
            case QUALITY -> "Qualité";
            case PRODUCTIVITY -> "Productivité";
            case EFFICIENCY -> "Efficacité";
        };
    }
    
    private String getRecommendationForMetricType(KPIMetric.MetricType type) {
        return switch (type) {
            case ATTENDANCE -> "Améliorer la ponctualité et la présence. Considérer des horaires flexibles si approprié.";
            case VELOCITY -> "Augmenter la vitesse d'exécution. Identifier les goulots d'étranglement et optimiser les processus.";
            case QUALITY -> "Renforcer le contrôle qualité. Fournir une formation supplémentaire sur les standards de qualité.";
            case PRODUCTIVITY -> "Améliorer la productivité. Examiner les outils et méthodes de travail pour optimiser l'efficacité.";
            case EFFICIENCY -> "Optimiser l'efficacité. Réduire le gaspillage de ressources et améliorer la gestion du temps.";
        };
    }
}


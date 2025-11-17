package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.entity.Employee;
import com.entreprise.kpi_analysis_Backend.entity.KPI;
import com.entreprise.kpi_analysis_Backend.entity.KPIMetric;
import com.entreprise.kpi_analysis_Backend.exception.ResourceNotFoundException;
import com.entreprise.kpi_analysis_Backend.repository.EmployeeRepository;
import com.entreprise.kpi_analysis_Backend.repository.KPIRepository;
import com.entreprise.kpi_analysis_Backend.repository.KPIMetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);
    
    private final KPIRepository kpiRepository;
    private final KPIMetricRepository kpiMetricRepository;
    private final EmployeeRepository employeeRepository;
    private final GroqService groqService;
    
    @Autowired
    public AIAnalysisService(
            KPIRepository kpiRepository, 
            KPIMetricRepository kpiMetricRepository,
            EmployeeRepository employeeRepository,
            GroqService groqService) {
        this.kpiRepository = kpiRepository;
        this.kpiMetricRepository = kpiMetricRepository;
        this.employeeRepository = employeeRepository;
        this.groqService = groqService;
    }
    
    /**
     * Analyze employee performance and generate AI analysis using Groq
     */
    public String analyzeEmployeePerformance(Long employeeId) {
        logger.info("Starting AI analysis for employee ID: {}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        
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
        
        try {
            // Format employee data for AI
            String employeeData = formatEmployeeData(employee);
            
            // Format KPI data for AI
            String kpiData = formatKPIData(kpis, allMetrics);
            
            // Use Groq AI for intelligent analysis
            logger.debug("Calling Groq API for employee analysis");
            String aiAnalysis = groqService.analyzeEmployeePerformance(employeeData, kpiData);
            
            // Check if the response is an error message
            if (aiAnalysis.startsWith("Erreur")) {
                logger.warn("Groq API returned error, falling back to rule-based analysis");
                return generateRuleBasedAnalysis(employeeId, kpis, allMetrics);
            }
            
            logger.info("Successfully generated AI analysis for employee: {}", employeeId);
            return aiAnalysis;
            
        } catch (Exception e) {
            logger.error("Error using Groq API, falling back to rule-based analysis: {}", e.getMessage(), e);
            return generateRuleBasedAnalysis(employeeId, kpis, allMetrics);
        }
    }
    
    /**
     * Fallback rule-based analysis when AI is unavailable
     */
    private String generateRuleBasedAnalysis(Long employeeId, List<KPI> kpis, List<KPIMetric> allMetrics) {
        Map<KPIMetric.MetricType, Double> averageScores = allMetrics.stream()
                .collect(Collectors.groupingBy(
                        KPIMetric::getMetricType,
                        Collectors.averagingDouble(metric -> 
                            metric.getTargetValue() != null && metric.getTargetValue() > 0
                                ? (metric.getValue() / metric.getTargetValue()) * 100
                                : metric.getValue()
                        )
                ));
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("Analyse de performance pour l'employé ID: ").append(employeeId).append("\n\n");
        
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
     * Generate AI recommendations based on KPI analysis using Groq
     */
    public String generateRecommendations(Long employeeId) {
        logger.info("Generating AI recommendations for employee ID: {}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        
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
        
        try {
            // First, get the analysis
            String analysis = analyzeEmployeePerformance(employeeId);
            
            // Format employee and KPI data
            String employeeData = formatEmployeeData(employee);
            String kpiData = formatKPIData(kpis, allMetrics);
            
            // Use Groq AI for intelligent recommendations
            logger.debug("Calling Groq API for recommendations");
            String aiRecommendations = groqService.generateRecommendations(employeeData, kpiData, analysis);
            
            // Check if the response is an error message
            if (aiRecommendations.startsWith("Erreur")) {
                logger.warn("Groq API returned error, falling back to rule-based recommendations");
                return generateRuleBasedRecommendations(employeeId, allMetrics);
            }
            
            logger.info("Successfully generated AI recommendations for employee: {}", employeeId);
            return aiRecommendations;
            
        } catch (Exception e) {
            logger.error("Error using Groq API, falling back to rule-based recommendations: {}", e.getMessage(), e);
            return generateRuleBasedRecommendations(employeeId, allMetrics);
        }
    }
    
    /**
     * Fallback rule-based recommendations when AI is unavailable
     */
    private String generateRuleBasedRecommendations(Long employeeId, List<KPIMetric> allMetrics) {
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("Recommandations pour l'employé ID: ").append(employeeId).append("\n\n");
        
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
    
    /**
     * Format employee data for AI analysis
     */
    private String formatEmployeeData(Employee employee) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom: ").append(employee.getFirstName()).append(" ").append(employee.getLastName()).append("\n");
        sb.append("Email: ").append(employee.getEmail()).append("\n");
        sb.append("Département: ").append(employee.getDepartment()).append("\n");
        sb.append("Poste: ").append(employee.getPosition()).append("\n");
        if (employee.getCreatedAt() != null) {
            sb.append("Date d'embauche: ").append(employee.getCreatedAt().toLocalDate()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Format KPI and metric data for AI analysis
     */
    private String formatKPIData(List<KPI> kpis, List<KPIMetric> allMetrics) {
        StringBuilder sb = new StringBuilder();
        
        // Group metrics by KPI ID - fetch metrics for each KPI to avoid lazy loading issues
        for (KPI kpi : kpis) {
            sb.append("--- Période KPI: ").append(kpi.getPeriodStart())
                    .append(" à ").append(kpi.getPeriodEnd()).append(" ---\n");
            
            if (kpi.getOverallScore() != null) {
                sb.append("Score global: ").append(String.format("%.2f%%", kpi.getOverallScore())).append("\n");
            }
            
            // Get metrics for this specific KPI using the repository (avoids lazy loading)
            List<KPIMetric> kpiMetrics = kpiMetricRepository.findByKpiId(kpi.getId());
            for (KPIMetric metric : kpiMetrics) {
                sb.append("  • ").append(getMetricTypeLabel(metric.getMetricType())).append(":\n");
                sb.append("    - Valeur: ").append(metric.getValue()).append("\n");
                if (metric.getTargetValue() != null && metric.getTargetValue() > 0) {
                    sb.append("    - Cible: ").append(metric.getTargetValue()).append("\n");
                    double percentage = (metric.getValue() / metric.getTargetValue()) * 100;
                    sb.append("    - Pourcentage: ").append(String.format("%.2f%%", percentage)).append("\n");
                }
                if (metric.getCreatedAt() != null) {
                    sb.append("    - Date de création: ").append(metric.getCreatedAt().toLocalDate()).append("\n");
                }
            }
            sb.append("\n");
        }
        
        // Summary by metric type
        sb.append("=== Résumé par Type de Métrique ===\n");
        Map<KPIMetric.MetricType, List<KPIMetric>> metricsByType = allMetrics.stream()
                .collect(Collectors.groupingBy(KPIMetric::getMetricType));
        
        for (Map.Entry<KPIMetric.MetricType, List<KPIMetric>> entry : metricsByType.entrySet()) {
            KPIMetric.MetricType type = entry.getKey();
            List<KPIMetric> metrics = entry.getValue();
            
            double average = metrics.stream()
                    .mapToDouble(metric -> {
                        if (metric.getTargetValue() != null && metric.getTargetValue() > 0) {
                            return (metric.getValue() / metric.getTargetValue()) * 100;
                        }
                        return metric.getValue();
                    })
                    .average()
                    .orElse(0.0);
            
            sb.append(getMetricTypeLabel(type)).append(": Moyenne de ")
                    .append(String.format("%.2f%%", average)).append("\n");
        }
        
        return sb.toString();
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


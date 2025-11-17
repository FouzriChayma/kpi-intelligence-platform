package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.dto.GroqRequest;
import com.entreprise.kpi_analysis_Backend.dto.GroqResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * Service for interacting with Groq AI API
 */
@Service
public class GroqService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroqService.class);
    
    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final ObjectMapper objectMapper;
    
    public GroqService(
            WebClient.Builder webClientBuilder,
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.url}") String apiUrl,
            @Value("${groq.model}") String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        
        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }
    
    /**
     * Generate AI response using Groq API
     * 
     * @param systemPrompt System prompt to set the AI's behavior
     * @param userPrompt User prompt with the actual request
     * @return AI-generated response
     */
    public String generateResponse(String systemPrompt, String userPrompt) {
        try {
            logger.debug("Sending request to Groq API with model: {}", model);
            
            GroqRequest.Message systemMessage = new GroqRequest.Message("system", systemPrompt);
            GroqRequest.Message userMessage = new GroqRequest.Message("user", userPrompt);
            
            GroqRequest request = new GroqRequest(model, List.of(systemMessage, userMessage));
            request.setTemperature(0.7);
            request.setMaxTokens(1500);
            
            GroqResponse response = webClient.post()
                    .uri("")  // Use empty URI since baseUrl is already set
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GroqResponse.class)
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2))
                            .filter(throwable -> {
                                logger.warn("Retrying Groq API call due to error: {}", throwable.getMessage());
                                return true;
                            }))
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                logger.info("Successfully received response from Groq API. Tokens used: {}", 
                    response.getUsage() != null ? response.getUsage().getTotalTokens() : "N/A");
                return content;
            } else {
                logger.error("Empty or invalid response from Groq API");
                return "Erreur: Réponse vide de l'API Groq.";
            }
            
        } catch (Exception e) {
            logger.error("Error calling Groq API: {}", e.getMessage(), e);
            return "Erreur lors de l'appel à l'API Groq: " + e.getMessage();
        }
    }
    
    /**
     * Generate AI analysis for employee performance
     */
    public String analyzeEmployeePerformance(String employeeData, String kpiData) {
        String systemPrompt = "Tu es un expert en analyse de performance RH et gestion des talents. " +
                "Ton rôle est d'analyser les indicateurs de performance (KPIs) des employés et de fournir " +
                "des analyses détaillées, objectives et constructives. Tu dois être précis, professionnel " +
                "et fournir des insights actionnables pour la prise de décision managériale.";
        
        String userPrompt = String.format(
            "Analyse les performances de l'employé suivant en te basant sur ses données KPI:\n\n" +
            "=== Informations Employé ===\n%s\n\n" +
            "=== Données KPI ===\n%s\n\n" +
            "Fournis une analyse détaillée qui inclut:\n" +
            "1. Un résumé exécutif de la performance globale\n" +
            "2. Une analyse par type de métrique (Assiduité, Vélocité, Qualité, etc.)\n" +
            "3. Les points forts identifiés\n" +
            "4. Les domaines nécessitant une amélioration\n" +
            "5. Une évaluation globale avec score contextuel\n\n" +
            "Sois précis, factuel et professionnel dans ton analyse.",
            employeeData, kpiData
        );
        
        return generateResponse(systemPrompt, userPrompt);
    }
    
    /**
     * Generate AI recommendations for employee improvement
     */
    public String generateRecommendations(String employeeData, String kpiData, String analysis) {
        String systemPrompt = "Tu es un consultant RH expert en développement des talents et amélioration " +
                "de la performance. Ton rôle est de fournir des recommandations concrètes, actionnables " +
                "et personnalisées pour améliorer la performance des employés. Tes recommandations doivent " +
                "être pratiques, mesurables et alignées avec les objectifs de l'entreprise.";
        
        String userPrompt = String.format(
            "Basé sur l'analyse suivante, génère des recommandations spécifiques et actionnables:\n\n" +
            "=== Informations Employé ===\n%s\n\n" +
            "=== Données KPI ===\n%s\n\n" +
            "=== Analyse de Performance ===\n%s\n\n" +
            "Fournis des recommandations qui incluent:\n" +
            "1. Des actions immédiates à entreprendre\n" +
            "2. Des objectifs de développement à court terme (1-3 mois)\n" +
            "3. Des objectifs de développement à moyen terme (3-6 mois)\n" +
            "4. Des ressources ou formations suggérées\n" +
            "5. Des indicateurs de succès pour mesurer l'amélioration\n\n" +
            "Sois spécifique, pratique et orienté résultats.",
            employeeData, kpiData, analysis
        );
        
        return generateResponse(systemPrompt, userPrompt);
    }
}


package com.entreprise.kpi_analysis_Backend.dto;

import java.util.List;
import java.util.Map;

public class FileUploadResponse {
    private boolean success;
    private String message;
    private int employeesProcessed;
    private int kpisCreated;
    private int metricsCreated;
    private List<String> warnings;
    private List<String> errors;
    private Map<String, Object> statistics;

    public FileUploadResponse() {}

    public FileUploadResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getEmployeesProcessed() {
        return employeesProcessed;
    }

    public void setEmployeesProcessed(int employeesProcessed) {
        this.employeesProcessed = employeesProcessed;
    }

    public int getKpisCreated() {
        return kpisCreated;
    }

    public void setKpisCreated(int kpisCreated) {
        this.kpisCreated = kpisCreated;
    }

    public int getMetricsCreated() {
        return metricsCreated;
    }

    public void setMetricsCreated(int metricsCreated) {
        this.metricsCreated = metricsCreated;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }
}


package com.entreprise.kpi_analysis_Backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GroqRequest {
    private String model;
    private List<Message> messages;
    private double temperature = 0.7;
    private int maxTokens = 1000;
    
    public GroqRequest() {}
    
    public GroqRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    @JsonProperty("max_tokens")
    public int getMaxTokens() {
        return maxTokens;
    }
    
    @JsonProperty("max_tokens")
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public static class Message {
        private String role;
        private String content;
        
        public Message() {}
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}


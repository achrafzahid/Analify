package com.analyfy.analify.DTO.Statistics;

import lombok.Data;
import java.util.List;

@Data
public class AnalyticsAssistantRequest {

    private String question;
    private List<ConversationMessage> conversationHistory;
    
    @Data
    public static class ConversationMessage {
        private String role; // "user" or "assistant"
        private String content;
    }

}

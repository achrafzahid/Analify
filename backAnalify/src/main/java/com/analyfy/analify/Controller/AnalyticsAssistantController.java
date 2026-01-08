package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.Statistics.AnalyticsAssistantRequest;
import com.analyfy.analify.DTO.Statistics.AnalyticsAssistantResponse;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Service.AnalyticsAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant/analytics")
@RequiredArgsConstructor
public class AnalyticsAssistantController {

    private final AnalyticsAssistantService analyticsAssistantService;

    @PostMapping("/query")
    public ResponseEntity<AnalyticsAssistantResponse> queryAnalyticsAssistant(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @RequestBody AnalyticsAssistantRequest request) {

        // Convert conversation history to Map format if present
        List<Map<String, String>> history = null;
        if (request.getConversationHistory() != null && !request.getConversationHistory().isEmpty()) {
            history = request.getConversationHistory().stream()
                    .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                    .toList();
        }
        
        AnalyticsAssistantResponse response = analyticsAssistantService.answerQuestionWithHistory(
                userId, role, request.getQuestion(), history);
        return ResponseEntity.ok(response);
    }
}

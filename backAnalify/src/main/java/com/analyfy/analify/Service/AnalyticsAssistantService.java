package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.Statistics.*;
import com.analyfy.analify.Enum.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AnalyticsAssistantService {

    private final EnhancedStatisticsService enhancedStatisticsService;
    private final StatisticsService statisticsService;
    private final ProductService productService;
    private final ChatClient chatClient;

    public AnalyticsAssistantService(
            EnhancedStatisticsService enhancedStatisticsService,
            StatisticsService statisticsService,
            ProductService productService,
            ChatModel chatModel) {
        this.enhancedStatisticsService = enhancedStatisticsService;
        this.statisticsService = statisticsService;
        this.productService = productService;
        
        // Build ChatClient with default system prompt
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                    You are an analytics assistant for a retail and bidding platform called Analify.
                    You receive summarized analytics data for the CURRENT authenticated user, already filtered by role.
                    Answer the user's question using ONLY this data.
                    Do not invent numbers or access any external resources.
                    If an exact value is not present, say that it is not available.
                    If the user is a CAISSIER (cashier), explain that they have limited access to analytics and can mainly manage orders.
                    Be concise and focus on business insights (revenue, orders, low stock, bidding, portfolio, etc.).
                    Format your response in a clean, readable manner. Use bullet points or short paragraphs where appropriate.
                    """)
                .build();
    }

    public AnalyticsAssistantResponse answerQuestion(Long userId, UserRole role, String question) {
        return answerQuestionWithHistory(userId, role, question, null);
    }
    
    public AnalyticsAssistantResponse answerQuestionWithHistory(Long userId, UserRole role, String question, List<Map<String, String>> conversationHistory) {
        if (question == null || question.isBlank()) {
            return AnalyticsAssistantResponse.builder()
                    .answer("Please provide a non-empty question.")
                    .metadata(Map.of("error", "EMPTY_QUESTION"))
                    .build();
        }

        try {
            // 1) Build analytics context using existing, role-safe services
            StatisticsFilterDTO filter = StatisticsFilterDTO.builder().build();
            
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("User Role: ").append(role.name()).append("\n\n");
            
            EnhancedDashboardDTO enhanced = null;
            DashboardStatsDTO basic = null;
            List<?> lowStockAlerts = null;
            
            // CAISSIER has limited access - only basic order-related info
            if (role == UserRole.CAISSIER) {
                contextBuilder.append("Note: This user is a Cashier with limited access to analytics.\n");
                contextBuilder.append("Cashiers can mainly manage orders and have no access to full business analytics.\n");
            } else {
                // Fetch and summarize enhanced dashboard
                try {
                    enhanced = enhancedStatisticsService.getEnhancedDashboard(userId, role, filter);
                    contextBuilder.append(summarizeEnhancedDashboard(enhanced));
                } catch (Exception e) {
                    log.warn("Could not fetch enhanced dashboard: {}", e.getMessage());
                }
                
                // Fetch and summarize basic dashboard
                try {
                    basic = statisticsService.getDashboard(userId, role, filter);
                    contextBuilder.append(summarizeBasicDashboard(basic));
                } catch (Exception e) {
                    log.warn("Could not fetch basic dashboard: {}", e.getMessage());
                }
                
                // Low stock alerts
                try {
                    lowStockAlerts = productService.getInvestorLowStockReport(userId, role, null);
                    if (lowStockAlerts != null && !lowStockAlerts.isEmpty()) {
                        contextBuilder.append("\n--- LOW STOCK ALERTS ---\n");
                        contextBuilder.append("Total low stock items: ").append(lowStockAlerts.size()).append("\n");
                        // Limit to first 10 for brevity
                        int count = Math.min(lowStockAlerts.size(), 10);
                        contextBuilder.append("First ").append(count).append(" items with low stock:\n");
                        for (int i = 0; i < count; i++) {
                            contextBuilder.append("  - ").append(lowStockAlerts.get(i).toString()).append("\n");
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch low stock alerts: {}", e.getMessage());
                }
            }

            // 2) Build the user prompt with summarized context
            String analyticsContext = contextBuilder.toString();
            String userPrompt = String.format("""
                Question: %s
                
                Analytics Data Summary:
                %s
                """, question, analyticsContext);

            // 3) Call the LLM using Spring AI ChatClient with conversation history
            var promptBuilder = chatClient.prompt()
                    .user(userPrompt);
            
            // Add conversation history if available (maintains context across questions)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                StringBuilder historyContext = new StringBuilder("\n\n--- Previous Conversation ---\n");
                int maxHistory = Math.min(conversationHistory.size(), 5); // Last 5 exchanges
                for (int i = conversationHistory.size() - maxHistory; i < conversationHistory.size(); i++) {
                    Map<String, String> msg = conversationHistory.get(i);
                    String msgRole = msg.getOrDefault("role", "user");
                    String content = msg.getOrDefault("content", "");
                    historyContext.append(msgRole.equals("user") ? "User: " : "Assistant: ")
                            .append(content).append("\n");
                }
                userPrompt = userPrompt + historyContext.toString();
            }
            
            String answer = promptBuilder.call().content();

            // 4) Build response metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("role", role.name());
            metadata.put("usedEnhancedDashboard", enhanced != null);
            metadata.put("usedBasicDashboard", basic != null);
            metadata.put("lowStockAlertsCount", lowStockAlerts != null ? lowStockAlerts.size() : 0);

            return AnalyticsAssistantResponse.builder()
                    .answer(answer)
                    .metadata(metadata)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to generate analytics assistant answer", e);
            
            String errorMessage = e.getMessage();
            String errorType = "LLM_CALL_FAILED";
            String userMessage = "Sorry, I couldn't process your analytics question right now. Please try again later.";
            
            if (errorMessage != null) {
                if (errorMessage.contains("429") || errorMessage.toLowerCase().contains("rate") 
                        || errorMessage.toLowerCase().contains("quota")) {
                    errorType = "QUOTA_EXCEEDED";
                    userMessage = "The AI service is currently rate-limited. Please wait a moment and try again.";
                } else if (errorMessage.contains("401") || errorMessage.contains("403") 
                        || errorMessage.toLowerCase().contains("unauthorized")) {
                    errorType = "AUTH_ERROR";
                    userMessage = "The AI service is not properly configured. Please contact the administrator.";
                }
            }
            
            return AnalyticsAssistantResponse.builder()
                    .answer(userMessage)
                    .metadata(Map.of("error", errorType))
                    .build();
        }
    }
    
    private String summarizeEnhancedDashboard(EnhancedDashboardDTO d) {
        if (d == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("--- ENHANCED DASHBOARD KPIs ---\n");
        
        // Core Metrics
        sb.append("\nKey Performance Indicators:\n");
        if (d.getTotalRevenue() != null) {
            sb.append("  - Total Revenue: ").append(formatCurrency(d.getTotalRevenue())).append("\n");
        }
        if (d.getTotalOrders() != null) {
            sb.append("  - Total Orders: ").append(d.getTotalOrders()).append("\n");
        }
        if (d.getTotalProductsSold() != null) {
            sb.append("  - Total Products Sold: ").append(d.getTotalProductsSold()).append("\n");
        }
        if (d.getAverageOrderValue() != null) {
            sb.append("  - Average Order Value: ").append(formatCurrency(d.getAverageOrderValue())).append("\n");
        }
        if (d.getTotalStockValue() != null) {
            sb.append("  - Total Stock Value: ").append(formatCurrency(d.getTotalStockValue())).append("\n");
        }
        if (d.getLowStockCount() != null) {
            sb.append("  - Low Stock Items Count: ").append(d.getLowStockCount()).append("\n");
        }
        
        // Bidding/Section Stats
        if (d.getTotalSections() != null || d.getActiveBiddingSections() != null || d.getTotalBids() != null) {
            sb.append("\nBidding Statistics:\n");
            if (d.getTotalSections() != null) {
                sb.append("  - Total Sections: ").append(d.getTotalSections()).append("\n");
            }
            if (d.getActiveBiddingSections() != null) {
                sb.append("  - Active Bidding Sections: ").append(d.getActiveBiddingSections()).append("\n");
            }
            if (d.getTotalBids() != null) {
                sb.append("  - Total Bids: ").append(d.getTotalBids()).append("\n");
            }
            if (d.getTotalSectionValue() != null) {
                sb.append("  - Total Section Value: ").append(formatCurrency(d.getTotalSectionValue())).append("\n");
            }
            if (d.getMyWonSections() != null) {
                sb.append("  - My Won Sections: ").append(d.getMyWonSections()).append("\n");
            }
            if (d.getMyTotalInvestment() != null) {
                sb.append("  - My Total Investment: ").append(formatCurrency(d.getMyTotalInvestment())).append("\n");
            }
        }
        
        // Top Products (limited to 5)
        if (d.getTopProducts() != null && !d.getTopProducts().isEmpty()) {
            sb.append("\nTop ").append(Math.min(5, d.getTopProducts().size())).append(" Products by Revenue:\n");
            d.getTopProducts().stream().limit(5).forEach(p -> 
                sb.append("  - ").append(p.getName())
                  .append(": ").append(formatCurrency(p.getValue()))
                  .append(p.getAdditionalInfo() != null ? " (" + p.getAdditionalInfo() + ")" : "")
                  .append("\n")
            );
        }
        
        // Top Stores (limited to 5)
        if (d.getTopStores() != null && !d.getTopStores().isEmpty()) {
            sb.append("\nTop ").append(Math.min(5, d.getTopStores().size())).append(" Stores by Revenue:\n");
            d.getTopStores().stream().limit(5).forEach(s -> 
                sb.append("  - ").append(s.getName())
                  .append(": ").append(formatCurrency(s.getValue())).append("\n")
            );
        }
        
        // Category Revenue Distribution
        if (d.getCategoryRevenueDistribution() != null && !d.getCategoryRevenueDistribution().isEmpty()) {
            sb.append("\nRevenue by Category:\n");
            d.getCategoryRevenueDistribution().forEach((cat, val) -> 
                sb.append("  - ").append(cat).append(": ").append(formatCurrency(val)).append("\n")
            );
        }
        
        // Sales by Region
        if (d.getSalesByRegion() != null && !d.getSalesByRegion().isEmpty()) {
            sb.append("\nSales by Region:\n");
            d.getSalesByRegion().forEach((region, val) -> 
                sb.append("  - ").append(region).append(": ").append(formatCurrency(val)).append("\n")
            );
        }
        
        // Sales by State (limit to top 5)
        if (d.getSalesByState() != null && !d.getSalesByState().isEmpty()) {
            sb.append("\nTop States by Sales:\n");
            d.getSalesByState().entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(e -> 
                    sb.append("  - ").append(e.getKey()).append(": ").append(formatCurrency(e.getValue())).append("\n")
                );
        }
        
        return sb.toString();
    }
    
    private String summarizeBasicDashboard(DashboardStatsDTO d) {
        if (d == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- BASIC DASHBOARD ---\n");
        
        if (d.getTotalRevenue() != null) {
            sb.append("Total Revenue: ").append(formatCurrency(d.getTotalRevenue())).append("\n");
        }
        if (d.getTotalOrders() != null) {
            sb.append("Total Orders: ").append(d.getTotalOrders()).append("\n");
        }
        if (d.getTotalProductsSold() != null) {
            sb.append("Total Products Sold: ").append(d.getTotalProductsSold()).append("\n");
        }
        if (d.getTotalStockValue() != null) {
            sb.append("Total Stock Value: ").append(formatCurrency(d.getTotalStockValue())).append("\n");
        }
        if (d.getLowStockCount() != null) {
            sb.append("Low Stock Count: ").append(d.getLowStockCount()).append("\n");
        }
        
        return sb.toString();
    }
    
    private String formatCurrency(Double amount) {
        if (amount == null) return "N/A";
        return String.format("$%,.2f", amount);
    }
}

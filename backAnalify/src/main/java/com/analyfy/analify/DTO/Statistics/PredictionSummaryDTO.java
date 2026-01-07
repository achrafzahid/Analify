package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PredictionSummaryDTO {
    // Revenue Predictions
    private Double predictedNextMonthRevenue;
    private Double predictedNextQuarterRevenue;
    private Double confidence; // 0.0 to 1.0
    
    // Stock Predictions
    private List<StockRecommendationDTO> stockRecommendations;
    private Integer predictedLowStockItems;
    
    // Bidding Predictions
    private Double predictedBiddingActivity;
    private List<String> hotSectionsToWatch;
    
    // Market Trends
    private String overallTrend; // BULLISH, BEARISH, NEUTRAL
    private String seasonalityFactor; // HIGH_SEASON, LOW_SEASON, NORMAL
    private List<String> opportunities; // Strategic recommendations
    private List<String> risks; // Warnings
}

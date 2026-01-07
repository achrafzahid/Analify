package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockRecommendationDTO {
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer recommendedStock;
    private String action; // RESTOCK, REDUCE, MAINTAIN
    private String priority; // HIGH, MEDIUM, LOW
    private Double estimatedCost;
    private String reason;
    private Integer predictedDemand; // Next 30 days
}

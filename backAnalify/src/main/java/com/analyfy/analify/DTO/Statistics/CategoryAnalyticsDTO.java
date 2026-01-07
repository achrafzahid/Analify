package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CategoryAnalyticsDTO {
    // Category Overview
    private String categoryName;
    private Long categoryId;
    
    // Hierarchy Stats
    private Long totalRangs;
    private Long totalFaces;
    private Long totalSections;
    
    // Product Stats
    private Long totalProducts;
    private Double totalProductValue;
    private Long lowStockProducts;
    
    // Section/Bidding Stats
    private Long activeSections;
    private Double totalSectionValue;
    private Long totalBids;
    private Double averageBidCompetition;
    
    // Revenue
    private Double totalRevenue; // From product sales
    private Double sectionRevenue; // From won sections
    private Double combinedRevenue;
    
    // Performance Metrics
    private Double growthRate; // Month over month
    private String performanceRating; // EXCELLENT, GOOD, AVERAGE, POOR
    private List<String> topPerformingRangs;
    private List<String> topPerformingFaces;
    
    // Predictions
    private Double predictedNextMonthRevenue;
    private String trend; // GROWING, STABLE, DECLINING
}

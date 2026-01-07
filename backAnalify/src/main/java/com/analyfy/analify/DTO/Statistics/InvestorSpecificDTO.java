package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class InvestorSpecificDTO {
    // Portfolio Overview
    private Long totalProductsOwned;
    private Double portfolioValue;
    private Long totalSectionsWon;
    private Double totalInvestmentInSections;
    
    // Performance
    private Double totalRevenuefromProducts;
    private Double totalRevenueFromSections;
    private Double portfolioGrowth; // Percentage
    private String performanceRating; // TOP_10_PERCENT, ABOVE_AVERAGE, AVERAGE, BELOW_AVERAGE
    
    // Active Bidding
    private Long activeBids;
    private Double totalBidAmount;
    private List<RankingItem> myTopBiddingSections;
    
    // Product Performance
    private List<RankingItem> bestSellingProducts;
    private List<RankingItem> worstSellingProducts;
    private Long lowStockAlerts;
    
    // Recommendations
    private List<String> recommendations;
    private List<String> sectionsToConsider; // Recommended sections to bid on
}

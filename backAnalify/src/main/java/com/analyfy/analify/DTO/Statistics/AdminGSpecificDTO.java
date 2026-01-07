package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminGSpecificDTO {
    // System Overview
    private Long totalUsers;
    private Long totalInvestors;
    private Long totalStores;
    private Long totalEmployees;
    
    // Platform Performance
    private Double platformRevenue;
    private Double platformGrowthRate;
    private Long totalTransactions;
    
    // Section Management
    private Long totalSectionsCreated;
    private Double totalSectionRevenue;
    private Double averageSectionCompetition;
    
    // User Activity
    private Map<String, Long> activeUsersByRole;
    private List<RankingItem> mostActiveInvestors;
    private List<RankingItem> topPerformingStores;
    
    // Category Performance
    private List<CategoryAnalyticsDTO> allCategoriesAnalytics;
    private Map<String, Double> categoryMarketShare;
    
    // System Health
    private Long pendingSections;
    private Long criticalLowStockItems;
    private List<String> systemAlerts;
    private String overallSystemHealth; // HEALTHY, GOOD, NEEDS_ATTENTION, CRITICAL
}

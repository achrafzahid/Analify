package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class EnhancedDashboardDTO {
    // === CORE METRICS (All Users) ===
    // Product Sales
    private Double totalRevenue;
    private Double totalStockValue;
    private Long totalOrders;
    private Long totalProductsSold;
    private Double averageOrderValue;
    private Long lowStockCount;
    
    // Section/Bidding (New)
    private Long totalSections;
    private Long activeBiddingSections;
    private Double totalSectionValue;
    private Long totalBids;
    private Long myWonSections; // For investors
    private Double myTotalInvestment; // For investors
    
    // === FINANCIAL OVERVIEW ===
    private FinancialSummaryDTO financialSummary;
    
    // === TIME SERIES ===
    private List<TimeSeriesPoint> revenueOverTime;
    private List<TimeSeriesPoint> bidsOverTime;
    private List<TimeSeriesPoint> sectionActivityOverTime;
    private Map<String, Long> ordersByDayOfWeek;
    private Map<String, Long> ordersByMonth;
    
    // === CATEGORY BREAKDOWN ===
    private Map<String, Double> categoryRevenueDistribution;
    private Map<String, Long> categoryProductCount;
    private List<CategoryAnalyticsDTO> topCategories;
    
    // === SECTION/BIDDING ANALYTICS ===
    private SectionStatsDTO sectionStats;
    
    // === GEOGRAPHIC ===
    private Map<String, Double> salesByRegion;
    private Map<String, Double> salesByState;
    
    // === LEADERBOARDS ===
    private List<RankingItem> topProducts;
    private List<RankingItem> topStores;
    private List<RankingItem> topInvestors;
    private List<RankingItem> topSections; // By value or bids
    
    // === PREDICTIONS & INSIGHTS ===
    private PredictionSummaryDTO predictions;
    private List<InsightDTO> insights; // AI-like insights
    
    // === ROLE-SPECIFIC DATA ===
    private InvestorSpecificDTO investorData; // Only for INVESTOR role
    private AdminGSpecificDTO adminGData; // Only for ADMIN_G role
}

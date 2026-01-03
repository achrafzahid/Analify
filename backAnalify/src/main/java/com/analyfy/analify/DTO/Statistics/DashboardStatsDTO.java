package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {
    // --- Cards ---
    private Double totalRevenue;
    private Double totalStockValue;
    private Long totalOrders;
    private Integer totalProductsSold;
    private Double averageOrderValue;
    private Long lowStockCount;

    // --- Time Charts ---
    private List<TimeSeriesPoint> revenueOverTime;
    private Map<String, Long> ordersByDayOfWeek;
    private Map<String, Long> ordersByMonth;

    // --- Categorical Charts ---
    private Map<String, Double> categoryRevenueDistribution;
    private Map<String, Long> categoryProductCount;

    // --- 🆕 Geographic Charts ---
    private Map<String, Double> salesByRegion; // e.g., "North": 5000.00
    private Map<String, Double> salesByState;  // e.g., "California": 2000.00

    // --- Leaderboards ---
    private List<RankingItem> topProducts;
    private List<RankingItem> topStores;
    private List<RankingItem> topInvestors;
}
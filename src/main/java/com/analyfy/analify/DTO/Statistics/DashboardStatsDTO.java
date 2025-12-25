package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {
    private Double totalRevenue;
    private Double totalStockValue; // Enhanced KPI
    private Long totalOrders;
    private Integer totalProductsSold;
    private Double averageOrderValue;
    private List<TimeSeriesPoint> revenueOverTime;
    private List<RankingItem> topProducts;
    private List<RankingItem> topStores;
    private List<RankingItem> topInvestors;
    private Map<String, Double> categoryDistribution;
}
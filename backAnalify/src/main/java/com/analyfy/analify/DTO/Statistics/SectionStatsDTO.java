package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SectionStatsDTO {
    // Overview Cards
    private Long totalSections;
    private Long activeSections;
    private Long closedSections;
    private Long wonSections;
    
    // Financial Metrics
    private Double totalSectionValue;
    private Double averageSectionPrice;
    private Double totalBidsValue;
    private Double averageBidIncrease; // Average increase from base to current price
    private Double expectedRevenue; // For active sections
    private Double actualRevenue; // For won sections
    
    // Bidding Activity
    private Long totalBids;
    private Double averageBidsPerSection;
    private Double bidWinRate; // Percentage of bids that won
    private List<RankingItem> mostActiveInvestors; // By number of bids
    private List<RankingItem> topBidders; // By total bid amounts
    
    // Section Performance
    private List<RankingItem> mostCompetitiveSections; // Most bids
    private List<RankingItem> highestValueSections; // Current price
    private Map<String, Long> sectionsByStatus; // OPEN, CLOSE, WON
    
    // Category/Face/Rang Breakdown
    private Map<String, Double> valueByCategory;
    private Map<String, Long> sectionsByCategory;
    private Map<String, Double> valueByFace;
    private Map<String, Double> valueByRang;
    
    // Time Series
    private List<TimeSeriesPoint> bidsOverTime;
    private List<TimeSeriesPoint> sectionValueOverTime;
    private Map<String, Long> sectionsOpenedByMonth;
    
    // Predictions
    private Double predictedNextMonthRevenue;
    private Double predictedBidActivity;
    private String marketTrend; // "HOT", "STABLE", "COOLING"
}

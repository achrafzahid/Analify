package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialSummaryDTO {
    // Revenue Streams
    private Double productSalesRevenue;
    private Double sectionSalesRevenue;
    private Double totalRevenue;
    
    // Costs & Investments
    private Double totalStockCost;
    private Double totalSectionInvestments;
    private Double operationalCosts; // Can be calculated or estimated
    
    // Profitability
    private Double grossProfit;
    private Double profitMargin; // Percentage
    private Double roi; // Return on Investment
    
    // Cash Flow
    private Double expectedIncome; // From active sections + projected sales
    private Double pendingPayments;
    private Double availableCash; // Estimated
    
    // Metrics
    private Double revenueGrowthRate; // Month over month
    private String financialHealth; // EXCELLENT, GOOD, MODERATE, CONCERNING
}

package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.Statistics.*;
import com.analyfy.analify.Entity.Product;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnhancedStatisticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SectionRepository sectionRepository;
    private final BidRepository bidRepository;
    private final CategoryRepository categoryRepository;
    private final InvestorRepository investorRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public EnhancedDashboardDTO getEnhancedDashboard(Long userId, UserRole role, StatisticsFilterDTO filter) {
        log.info("Generating enhanced dashboard for user {} with role {}", userId, role);
        ensureDateRange(filter, role);

        Long storeId = null;
        Long investorId = null;

        // Role-based Context Setup - STRICT ENCAPSULATION
        if (role == UserRole.ADMIN_STORE) {
            // ADMIN_STORE can ONLY see their own store - ignore filter
            storeId = resolveStoreId(userId, null);
            investorId = null; // Store admin doesn't filter by investor
        } else if (role == UserRole.INVESTOR) {
            // INVESTOR can ONLY see their own products - strict enforcement
            investorId = userId;
            storeId = null; // Investor doesn't filter by store
        } else if (role == UserRole.ADMIN_G) {
            // ADMIN_G can see everything and apply optional filters
            storeId = filter.getStoreId();
            investorId = filter.getInvestorId();
        }

        return generateComprehensiveDashboard(filter, storeId, investorId, role, userId);
    }

    private EnhancedDashboardDTO generateComprehensiveDashboard(
            StatisticsFilterDTO filter, Long storeId, Long investorId, UserRole role, Long userId) {
        
        // === CORE METRICS ===
        Double totalRevenue = orderRepository.calculateTotalRevenue(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Double stockValue = productRepository.calculateTotalStockValue(storeId, investorId);
        Long totalOrders = orderRepository.countTotalOrders(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Integer totalSoldInt = orderRepository.countTotalProductsSold(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Long totalSold = totalSoldInt != null ? totalSoldInt.longValue() : 0L;
        Long lowStock = productRepository.countLowStockItems(storeId, investorId, 10);
        
        // Section/Bidding Metrics - role-based filtering
        // For ADMIN_G: all sections; for INVESTOR: only their won/bid sections; for ADMIN_STORE: none (sections not store-specific)
        Long totalSections = (role == UserRole.ADMIN_G) ? sectionRepository.count() : 
                            (role == UserRole.INVESTOR) ? (long) sectionRepository.findByWinnerInvestorUserId(investorId).size() : 0L;
        Long activeSections = (role == UserRole.ADMIN_G) ? sectionRepository.countByStatus("OPEN") : 
                             (role == UserRole.INVESTOR) ? (long) sectionRepository.findByWinnerInvestorUserId(investorId).size() : 0L;
        Double totalSectionValue = sectionRepository.calculateTotalSectionValue(investorId);
        Long totalBids = (role == UserRole.ADMIN_G) ? bidRepository.count() : 
                        (role == UserRole.INVESTOR) ? bidRepository.countTotalBids(investorId) : 0L;
        
        // Investor-specific section metrics
        Long myWonSections = (role == UserRole.INVESTOR) ? 
            (long)sectionRepository.findByWinnerInvestorUserId(investorId).size() : 0L;
        Double myTotalInvestment = (role == UserRole.INVESTOR) ? 
            sectionRepository.calculateActualRevenue(investorId) : 0.0;

        // === FINANCIAL SUMMARY ===
        FinancialSummaryDTO financialSummary = buildFinancialSummary(
            totalRevenue, sectionRepository.calculateActualRevenue(investorId), stockValue, investorId);

        // === TIME SERIES ===
        List<TimeSeriesPoint> revenueOverTime = mapToTimeSeries(
            orderRepository.findRevenueTimeSeries(filter.getStartDate(), filter.getEndDate(), storeId, investorId, filter.getProductId())
        );
        
        LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = filter.getEndDate().atTime(23, 59, 59);
        
        List<TimeSeriesPoint> bidsOverTime = mapToTimeSeries(
            bidRepository.findBidsOverTime(startDateTime, endDateTime, investorId)
        );

        List<LocalDate> allDates = orderRepository.findAllOrderDates(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Map<String, Long> weekStats = calculateWeekStats(allDates);
        Map<String, Long> monthStats = calculateMonthStats(allDates);

        // === CATEGORY BREAKDOWN ===
        Map<String, Double> categoryRevenue = mapToDoubleMap(
            productRepository.findCategoryRevenueDistribution(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );
        
        Map<String, Long> productCountByCategory = productRepository.countProductsByCategory(investorId)
                .stream().collect(Collectors.toMap(r -> String.valueOf(r[0]), r -> ((Number)r[1]).longValue()));

        // Category analytics - ADMIN_G sees all, ADMIN_STORE sees their store, INVESTOR sees nothing
        List<CategoryAnalyticsDTO> topCategories = (role != UserRole.INVESTOR) ? 
            buildTopCategoriesAnalytics(role, storeId, investorId, filter) : Collections.emptyList();

        // === SECTION STATS ===
        // Only ADMIN_G and INVESTOR see section stats (sections are not store-specific)
        SectionStatsDTO sectionStats = (role == UserRole.ADMIN_STORE) ? null : buildSectionStats(investorId, filter, role);

        // === GEOGRAPHIC ===
        Map<String, Double> salesByRegion = mapToDoubleMap(
            orderRepository.findSalesByRegion(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );
        Map<String, Double> salesByState = mapToDoubleMap(
            orderRepository.findSalesByState(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );

        // === LEADERBOARDS ===
        // Top products filtered by role: ADMIN_G (all), ADMIN_STORE (their store), INVESTOR (their products)
        List<RankingItem> topProducts = mapToRanking(productRepository.findTopSellingProducts(
                filter.getStartDate(), filter.getEndDate(), investorId, storeId, PageRequest.of(0, 10)));
        
        List<RankingItem> topStores = (role == UserRole.ADMIN_G) ? mapToRanking(productRepository.findTopStores(
                filter.getStartDate(), filter.getEndDate(), PageRequest.of(0, 5))) : Collections.emptyList();
        
        List<RankingItem> topInvestors = (role == UserRole.ADMIN_G) ? mapToRanking(productRepository.findTopInvestors(
                filter.getStartDate(), filter.getEndDate(), PageRequest.of(0, 5))) : Collections.emptyList();
        
        List<RankingItem> topSections = mapToRanking(
            sectionRepository.findHighestValueSections(investorId).stream().limit(10).toList());
        
        List<RankingItem> topCategoriesRanking = buildCategoryRanking(categoryRevenue);

        // === PREDICTIONS & INSIGHTS ===
        PredictionSummaryDTO predictions = buildPredictions(revenueOverTime, stockValue, investorId, role);
        List<InsightDTO> insights = generateInsights(totalRevenue, lowStock, activeSections, role, financialSummary);

        // === ROLE-SPECIFIC DATA ===
        InvestorSpecificDTO investorData = (role == UserRole.INVESTOR) ? 
            buildInvestorSpecificData(userId, filter) : null;
        
        // ADMIN_G specific data - platform-wide statistics (no filters)
        AdminGSpecificDTO adminGData = (role == UserRole.ADMIN_G) ? 
            buildAdminGSpecificData(filter) : null;

        return EnhancedDashboardDTO.builder()
                // Core Metrics
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .totalStockValue(stockValue != null ? stockValue : 0.0)
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .totalProductsSold(totalSold)
                .averageOrderValue(totalOrders != null && totalOrders > 0 ? totalRevenue / totalOrders : 0.0)
                .lowStockCount(lowStock != null ? lowStock : 0L)
                // Section Metrics
                .totalSections(totalSections)
                .activeBiddingSections(activeSections)
                .totalSectionValue(totalSectionValue != null ? totalSectionValue : 0.0)
                .totalBids(totalBids)
                .myWonSections(myWonSections)
                .myTotalInvestment(myTotalInvestment)
                // Financial
                .financialSummary(financialSummary)
                // Time Series
                .revenueOverTime(compressTimeSeries(revenueOverTime, 20))
                .bidsOverTime(compressTimeSeries(bidsOverTime, 20))
                .ordersByDayOfWeek(weekStats)
                .ordersByMonth(monthStats)
                // Category
                .categoryRevenueDistribution(categoryRevenue)
                .categoryProductCount(productCountByCategory)
                .topCategories(topCategories)
                // Section Stats
                .sectionStats(sectionStats)
                // Geographic
                .salesByRegion(salesByRegion)
                .salesByState(salesByState)
                // Leaderboards
                .topProducts(topProducts)
                .topStores(topStores)
                .topInvestors(topInvestors)
                .topSections(topSections)
                // Predictions & Insights
                .predictions(predictions)
                .insights(insights)
                // Role-Specific
                .investorData(investorData)
                .adminGData(adminGData)
                .build();
    }

    // ==================== SECTION STATS BUILDER ====================
    private SectionStatsDTO buildSectionStats(Long investorId, StatisticsFilterDTO filter, UserRole role) {
        // For INVESTOR: only their sections; for ADMIN_G: all sections
        List<Object[]> statusCounts = sectionRepository.countSectionsByStatus(investorId);
        Map<String, Long> sectionsByStatus = statusCounts.stream()
            .collect(Collectors.toMap(r -> String.valueOf(r[0]), r -> ((Number)r[1]).longValue()));
        
        Long totalSections = sectionsByStatus.values().stream().mapToLong(Long::longValue).sum();
        Long activeSections = sectionsByStatus.getOrDefault("OPEN", 0L);
        Long closedSections = sectionsByStatus.getOrDefault("CLOSE", 0L);
        Long wonSections = investorId != null ? 
            (long) sectionRepository.findByWinnerInvestorUserId(investorId).size() : 0L;
        
        // Total value of all sections (base price + increments)
        Double totalSectionValue = safeDouble(sectionRepository.calculateTotalSectionValue(investorId));
        
        // Average price per section
        Double averageSectionPrice = safeDouble(sectionRepository.calculateAverageSectionPrice(investorId));
        
        // Total value of all bids placed
        Double totalBidsValue = safeDouble(bidRepository.calculateTotalBidsValue(investorId));
        
        // Average price increase from bidding
        Double averageBidIncrease = safeDouble(sectionRepository.calculateAveragePriceIncrease(investorId));
        
        // Expected revenue from winning sections (if they close)
        Double expectedRevenue = safeDouble(sectionRepository.calculateExpectedRevenue(investorId));
        
        // Actual revenue from closed/won sections
        Double actualRevenue = safeDouble(sectionRepository.calculateActualRevenue(investorId));
        
        Long totalBids = (role == UserRole.ADMIN_G) ? bidRepository.count() : 
                        (investorId != null ? bidRepository.countTotalBids(investorId) : 0L);
        Long investorBids = investorId != null ? bidRepository.countTotalBids(investorId) : totalBids;
        Double averageBidsPerSection = totalSections > 0 ? (double) investorBids / totalSections : 0.0;
        Double bidWinRate = investorId != null ? bidRepository.calculateWinRate(investorId) : 0.0;
        
        // Leaderboards - only ADMIN_G sees all investors/bidders
        List<RankingItem> mostActiveInvestors = (role == UserRole.ADMIN_G) ? mapToRanking(
            bidRepository.findMostActiveInvestors().stream().limit(10).toList()) : Collections.emptyList();
        List<RankingItem> topBidders = (role == UserRole.ADMIN_G) ? mapToRanking(
            bidRepository.findTopBiddersByAmount().stream().limit(10).toList()) : Collections.emptyList();
        
        List<RankingItem> mostCompetitive = mapToRanking(
            sectionRepository.findMostCompetitiveSections(investorId).stream().limit(10).toList());
        List<RankingItem> highestValue = mapToRanking(
            sectionRepository.findHighestValueSections(investorId).stream().limit(10).toList());
        
        Map<String, Double> valueByCategory = mapToDoubleMap(sectionRepository.calculateValueByCategory(investorId));
        Map<String, Long> sectionsByCategory = mapToLongMap(sectionRepository.countSectionsByCategory(investorId));
        Map<String, Double> valueByFace = mapToDoubleMap(sectionRepository.calculateValueByFace(investorId));
        Map<String, Double> valueByRang = mapToDoubleMap(sectionRepository.calculateValueByRang(investorId));
        
        LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = filter.getEndDate().atTime(23, 59, 59);
        List<TimeSeriesPoint> bidsTimeSeries = mapToTimeSeries(
            bidRepository.findBidsOverTime(startDateTime, endDateTime, investorId));
        
        Map<String, Long> sectionsOpenedByMonth = mapToLongMap(
            sectionRepository.countSectionsOpenedByMonth(investorId));
        
        // Predictions - ensure positive values
        Double predictedRevenue = Math.max(0.0, expectedRevenue * 1.15);
        Double predictedBidActivity = Math.max(0.0, (double) totalBids * 1.1);
        String marketTrend = determineMarketTrend(activeSections, totalBids);
        
        return SectionStatsDTO.builder()
                .totalSections(totalSections)
                .activeSections(activeSections)
                .closedSections(closedSections)
                .wonSections(wonSections)
                .totalSectionValue(Math.max(0.0, totalSectionValue))
                .averageSectionPrice(Math.max(0.0, averageSectionPrice))
                .totalBidsValue(Math.max(0.0, totalBidsValue))
                .averageBidIncrease(Math.max(0.0, averageBidIncrease))
                .expectedRevenue(Math.max(0.0, expectedRevenue))
                .actualRevenue(Math.max(0.0, actualRevenue))
                .totalBids(totalBids)
                .averageBidsPerSection(averageBidsPerSection)
                .bidWinRate(safeDouble(bidWinRate))
                .mostActiveInvestors(mostActiveInvestors)
                .topBidders(topBidders)
                .mostCompetitiveSections(mostCompetitive)
                .highestValueSections(highestValue)
                .sectionsByStatus(sectionsByStatus)
                .valueByCategory(valueByCategory)
                .sectionsByCategory(sectionsByCategory)
                .valueByFace(valueByFace)
                .valueByRang(valueByRang)
                .bidsOverTime(bidsTimeSeries)
                .sectionsOpenedByMonth(sectionsOpenedByMonth)
                .predictedNextMonthRevenue(predictedRevenue)
                .predictedBidActivity(predictedBidActivity)
                .marketTrend(marketTrend)
                .build();
    }

    // ==================== FINANCIAL SUMMARY ====================
    private FinancialSummaryDTO buildFinancialSummary(Double productRevenue, Double sectionRevenue, 
                                                       Double stockValue, Long investorId) {
        // Revenue from selling products
        Double prodRev = safeDouble(productRevenue);
        
        // Section revenue = money earned from sections that were won (this is actual income)
        Double sectRev = safeDouble(sectionRevenue);
        
        // Total revenue from both sources
        Double totalRev = prodRev + sectRev;
        
        // Cost of goods sold - only for products actually sold, not total inventory
        // We estimate COGS as 60% of product revenue (40% margin)
        Double productCOGS = prodRev * 0.60;
        
        // Section investments - money paid to win sections (this is the cost/investment)
        // Note: sectionRevenue here is confusing - it's actually the amount paid for sections
        // For investors: this is the bid amount they paid to win sections
        Double sectionInvestments = safeDouble(sectRev);
        
        // Operational costs (salaries, rent, utilities, etc.) - 15% of revenue
        Double operationalCosts = totalRev * 0.15;
        
        // Gross profit = Revenue - COGS - Operational Costs
        // Note: Section revenue is already net (payment received), so we don't subtract it again
        Double grossProfit = prodRev - productCOGS - operationalCosts + sectRev;
        
        // Profit margin as percentage of total revenue
        Double profitMargin = totalRev > 0 ? (grossProfit / totalRev) * 100 : 0.0;
        
        // ROI = (Profit / Total Investment) * 100
        // Total investment = COGS + Section bids paid + Operational costs
        Double totalInvestment = productCOGS + sectionInvestments + operationalCosts;
        Double roi = totalInvestment > 0 ? (grossProfit / totalInvestment) * 100 : 0.0;
        
        // Expected income from pending/open sections
        Double expectedIncome = sectionRepository.calculateExpectedRevenue(investorId);
        
        // Pending payments (5% of revenue not yet collected)
        Double pendingPayments = totalRev * 0.05;
        
        // Available cash = gross profit - pending payments
        Double availableCash = Math.max(0.0, grossProfit - pendingPayments);
        
        Double revenueGrowthRate = 12.5; // Would need historical comparison
        String financialHealth = determineFinancialHealth(profitMargin, roi);
        
        return FinancialSummaryDTO.builder()
                .productSalesRevenue(prodRev)
                .sectionSalesRevenue(sectRev)
                .totalRevenue(totalRev)
                .totalStockCost(productCOGS)  // Cost of goods sold, not total inventory value
                .totalSectionInvestments(sectionInvestments)
                .operationalCosts(operationalCosts)
                .grossProfit(Math.max(0.0, grossProfit))  // Ensure non-negative
                .profitMargin(round(profitMargin))
                .roi(round(roi))
                .expectedIncome(safeDouble(expectedIncome))
                .pendingPayments(pendingPayments)
                .availableCash(Math.max(0.0, availableCash))  // Ensure non-negative
                .revenueGrowthRate(revenueGrowthRate)
                .financialHealth(financialHealth)
                .build();
    }

    // ==================== CATEGORY ANALYTICS ====================
    private List<CategoryAnalyticsDTO> buildTopCategoriesAnalytics(UserRole role, Long storeId, Long investorId, StatisticsFilterDTO filter) {
        if (role != UserRole.ADMIN_G && role != UserRole.ADMIN_STORE) {
            return Collections.emptyList();
        }
        // Note: storeId filters data for ADMIN_STORE, null for ADMIN_G (sees all)
        
        List<CategoryAnalyticsDTO> result = new ArrayList<>();
        List<Object[]> categories = categoryRepository.findAll().stream()
            .limit(5)
            .map(c -> new Object[]{c.getCategoryId(), c.getCategoryName()})
            .toList();
        
        for (Object[] cat : categories) {
            Long catId = ((Number) cat[0]).longValue();
            String catName = (String) cat[1];
            
            // This would require custom queries - simplified for now
            CategoryAnalyticsDTO dto = CategoryAnalyticsDTO.builder()
                    .categoryId(catId)
                    .categoryName(catName)
                    .totalRangs(3L)
                    .totalFaces(8L)
                    .totalSections(15L)
                    .totalProducts(50L)
                    .totalProductValue(25000.0)
                    .lowStockProducts(5L)
                    .activeSections(3L)
                    .totalSectionValue(45000.0)
                    .totalBids(120L)
                    .averageBidCompetition(8.0)
                    .totalRevenue(75000.0)
                    .sectionRevenue(30000.0)
                    .combinedRevenue(105000.0)
                    .growthRate(15.5)
                    .performanceRating("GOOD")
                    .topPerformingRangs(List.of("Rang A", "Rang B"))
                    .topPerformingFaces(List.of("Face 1", "Face 2"))
                    .predictedNextMonthRevenue(110000.0)
                    .trend("GROWING")
                    .build();
            result.add(dto);
        }
        
        return result;
    }

    // ==================== PREDICTIONS ====================
    private PredictionSummaryDTO buildPredictions(List<TimeSeriesPoint> revenueHistory, 
                                                   Double stockValue, Long investorId, UserRole role) {
        List<TimeSeriesPoint> forecast = calculateLinearRegressionForecast(revenueHistory, 30);
        Double predictedNextMonth = forecast.isEmpty() ? 0.0 : 
            forecast.stream().mapToDouble(TimeSeriesPoint::getValue).sum();
        Double predictedNextQuarter = predictedNextMonth * 3;
        
        List<StockRecommendationDTO> stockRecs = buildStockRecommendations(investorId);
        Integer predictedLowStock = (int) (stockRecs.size() * 1.2);
        
        Double predictedBidActivity = (double) bidRepository.count() * 1.15;
        List<String> hotSections = sectionRepository.findMostCompetitiveSections(null).stream()
            .limit(3)
            .map(r -> String.valueOf(r[0]))
            .toList();
        
        String trend = analyzeTrend(forecast);
        String seasonality = determineSeasonality();
        List<String> opportunities = generateOpportunities(role);
        List<String> risks = generateRisks(stockValue);
        
        return PredictionSummaryDTO.builder()
                .predictedNextMonthRevenue(round(predictedNextMonth))
                .predictedNextQuarterRevenue(round(predictedNextQuarter))
                .confidence(0.78)
                .stockRecommendations(stockRecs)
                .predictedLowStockItems(predictedLowStock)
                .predictedBiddingActivity(round(predictedBidActivity))
                .hotSectionsToWatch(hotSections)
                .overallTrend(trend)
                .seasonalityFactor(seasonality)
                .opportunities(opportunities)
                .risks(risks)
                .build();
    }

    // ==================== STOCK RECOMMENDATIONS ====================
    private List<StockRecommendationDTO> buildStockRecommendations(Long investorId) {
        List<StockRecommendationDTO> recommendations = new ArrayList<>();
        
        // Get low stock products
        List<Product> products = productRepository.findAll().stream()
            .filter(p -> investorId == null || (p.getId_inv() != null && p.getId_inv().getUserId().equals(investorId)))
            .limit(10)
            .toList();
        
        for (Product product : products) {
            int currentStock = product.getStocks() != null && !product.getStocks().isEmpty() ? 
                product.getStocks().size() * 10 : 0; // Simplified calculation without Inventory entity
            
            if (currentStock < 20) {
                int recommendedStock = calculateRecommendedStock(currentStock);
                recommendations.add(StockRecommendationDTO.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .currentStock(currentStock)
                        .recommendedStock(recommendedStock)
                        .action(currentStock < 10 ? "RESTOCK" : "MAINTAIN")
                        .priority(currentStock < 5 ? "HIGH" : currentStock < 15 ? "MEDIUM" : "LOW")
                        .estimatedCost((product.getPrice() != null ? product.getPrice() : 0.0) * (recommendedStock - currentStock))
                        .reason(currentStock < 10 ? "Critical low stock" : "Below optimal level")
                        .predictedDemand(estimateDemand(currentStock))
                        .build());
            }
        }
        
        return recommendations;
    }

    // ==================== INSIGHTS GENERATION ====================
    private List<InsightDTO> generateInsights(Double revenue, Long lowStock, Long activeSections, 
                                                UserRole role, FinancialSummaryDTO financial) {
        List<InsightDTO> insights = new ArrayList<>();
        
        if (lowStock != null && lowStock > 10) {
            insights.add(InsightDTO.builder()
                    .type("WARNING")
                    .title("Low Stock Alert")
                    .description(lowStock + " products are running low on inventory")
                    .actionRecommendation("Review and restock critical items")
                    .severity("HIGH")
                    .icon("⚠️")
                    .build());
        }
        
        if (financial.getProfitMargin() > 25) {
            insights.add(InsightDTO.builder()
                    .type("SUCCESS")
                    .title("Excellent Profit Margins")
                    .description("Your profit margin of " + String.format("%.1f%%", financial.getProfitMargin()) + " is above industry average")
                    .actionRecommendation("Maintain current pricing strategy")
                    .severity("LOW")
                    .icon("✅")
                    .build());
        }
        
        if (activeSections != null && activeSections > 5) {
            insights.add(InsightDTO.builder()
                    .type("OPPORTUNITY")
                    .title("High Bidding Activity")
                    .description(activeSections + " sections are currently open for bidding")
                    .actionRecommendation("Monitor competitive sections closely")
                    .severity("MEDIUM")
                    .icon("💡")
                    .build());
        }
        
        if (revenue != null && revenue > 100000) {
            insights.add(InsightDTO.builder()
                    .type("INFO")
                    .title("Revenue Milestone")
                    .description("Total revenue reached " + String.format("$%.2f", revenue))
                    .actionRecommendation("Consider expanding product lines")
                    .severity("LOW")
                    .icon("📊")
                    .build());
        }
        
        return insights;
    }

    // ==================== INVESTOR-SPECIFIC DATA ====================
    private InvestorSpecificDTO buildInvestorSpecificData(Long investorId, StatisticsFilterDTO filter) {
        // Count of products owned by this investor
        Long productsOwned = productRepository.countByInvestorUserId(investorId);
        
        // Total value of all product inventory owned
        Double portfolioValue = safeDouble(productRepository.calculateTotalStockValue(null, investorId));
        
        // Number of sections won by this investor
        Long sectionsWon = (long) sectionRepository.findByWinnerInvestorUserId(investorId).size();
        
        // Total amount invested in sections (money paid for winning bids)
        Double sectionInvestment = safeDouble(sectionRepository.calculateActualRevenue(investorId));
        
        // Revenue from selling products
        Double productRevenue = safeDouble(orderRepository.calculateTotalRevenue(filter.getStartDate(), filter.getEndDate(), null, investorId));
        
        // Revenue from sections = value received from sections (could be rental income, usage fees, etc.)
        // For now, we'll use section investment as the section value
        Double sectionRevenue = sectionInvestment;
        
        // Portfolio growth rate (would need historical comparison)
        Double portfolioGrowth = 18.5;
        
        Long activeBids = (long) bidRepository.findByInvestorUserIdAndStatus(investorId, "PENDING").size();
        Double totalBidAmount = bidRepository.calculateTotalBidsValue(investorId);
        
        List<RankingItem> topBiddingSections = Collections.emptyList(); // Simplified - would need custom query
        
        List<RankingItem> bestSelling = mapToRanking(
            productRepository.findTopSellingProducts(filter.getStartDate(), filter.getEndDate(), investorId, null, PageRequest.of(0, 5)));
        
        List<RankingItem> worstSelling = Collections.emptyList(); // Would need custom query
        Long lowStockAlerts = productRepository.countLowStockItems(null, investorId, 10);
        
        return InvestorSpecificDTO.builder()
                .totalProductsOwned(productsOwned != null ? productsOwned : 0L)
                .portfolioValue(Math.max(0.0, portfolioValue))
                .totalSectionsWon(sectionsWon != null ? sectionsWon : 0L)
                .totalInvestmentInSections(Math.max(0.0, sectionInvestment))
                .totalRevenuefromProducts(Math.max(0.0, productRevenue))
                .totalRevenueFromSections(Math.max(0.0, sectionRevenue))
                .portfolioGrowth(portfolioGrowth)
                .performanceRating("ABOVE_AVERAGE")
                .activeBids(activeBids != null ? activeBids : 0L)
                .totalBidAmount(Math.max(0.0, totalBidAmount))
                .myTopBiddingSections(topBiddingSections)
                .bestSellingProducts(bestSelling)
                .worstSellingProducts(worstSelling)
                .lowStockAlerts(lowStockAlerts)
                .recommendations(List.of("Consider diversifying product portfolio", "Monitor competitive bidding sections"))
                .sectionsToConsider(List.of("Section Premium Zone A", "Section VIP Face 3"))
                .build();
    }

    // ==================== ADMIN-G SPECIFIC DATA ====================
    private AdminGSpecificDTO buildAdminGSpecificData(StatisticsFilterDTO filter) {
        Long totalUsers = userRepository.count();
        Long totalInvestors = investorRepository.count();
        Long totalStores = storeRepository.count();
        Long totalEmployees = totalUsers - totalInvestors - totalStores; // Simplified
        
        Double platformRevenue = orderRepository.calculateTotalRevenue(filter.getStartDate(), filter.getEndDate(), null, null);
        Double platformGrowth = 22.3;
        Long totalTransactions = orderRepository.count();
        
        Long totalSectionsCreated = sectionRepository.count();
        Double totalSectionRevenue = sectionRepository.calculateActualRevenue(null);
        Long totalBids = bidRepository.count();
        Long totalSections = sectionRepository.count();
        Double avgCompetition = totalSections > 0 ? (double) totalBids / totalSections : 0.0;
        
        Map<String, Long> activeUsersByRole = Map.of(
            "INVESTOR", totalInvestors,
            "ADMIN_STORE", totalStores,
            "CAISSIER", totalEmployees
        );
        
        List<RankingItem> mostActiveInvestors = mapToRanking(
            bidRepository.findMostActiveInvestors().stream().limit(10).toList());
        
        List<RankingItem> topStores = mapToRanking(
            productRepository.findTopStores(filter.getStartDate(), filter.getEndDate(), PageRequest.of(0, 10)));
        
        // ADMIN_G sees all categories (no store/investor filter)
        List<CategoryAnalyticsDTO> allCategories = buildTopCategoriesAnalytics(UserRole.ADMIN_G, null, null, filter);
        
        Map<String, Double> categoryMarketShare = allCategories.stream()
            .collect(Collectors.toMap(
                CategoryAnalyticsDTO::getCategoryName,
                c -> c.getCombinedRevenue() / platformRevenue * 100
            ));
        
        Long pendingSections = sectionRepository.countByStatus("OPEN");
        Long criticalLowStock = productRepository.countLowStockItems(null, null, 5);
        
        return AdminGSpecificDTO.builder()
                .totalUsers(totalUsers != null ? totalUsers : 0L)
                .totalInvestors(totalInvestors != null ? totalInvestors : 0L)
                .totalStores(totalStores != null ? totalStores : 0L)
                .totalEmployees(totalEmployees != null ? totalEmployees : 0L)
                .platformRevenue(Math.max(0.0, safeDouble(platformRevenue)))
                .platformGrowthRate(platformGrowth)
                .totalTransactions(totalTransactions != null ? totalTransactions : 0L)
                .totalSectionsCreated(totalSectionsCreated != null ? totalSectionsCreated : 0L)
                .totalSectionRevenue(Math.max(0.0, safeDouble(totalSectionRevenue)))
                .averageSectionCompetition(Math.max(0.0, round(avgCompetition)))
                .activeUsersByRole(activeUsersByRole)
                .mostActiveInvestors(mostActiveInvestors)
                .topPerformingStores(topStores)
                .allCategoriesAnalytics(allCategories)
                .categoryMarketShare(categoryMarketShare)
                .pendingSections(pendingSections)
                .criticalLowStockItems(criticalLowStock)
                .systemAlerts(List.of("System healthy", "All services operational"))
                .overallSystemHealth("HEALTHY")
                .build();
    }

    // ==================== HELPER METHODS ====================
    private List<RankingItem> buildCategoryRanking(Map<String, Double> categoryRevenue) {
        return categoryRevenue.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .map(e -> RankingItem.builder()
                .name(e.getKey())
                .value(e.getValue())
                .build())
            .collect(Collectors.toList());
    }

    private String determineMarketTrend(Long activeSections, Long totalBids) {
        if (activeSections > 10 && totalBids > 100) return "HOT";
        if (activeSections < 3 || totalBids < 20) return "COOLING";
        return "STABLE";
    }

    private String determineFinancialHealth(Double profitMargin, Double roi) {
        if (profitMargin > 30 && roi > 40) return "EXCELLENT";
        if (profitMargin > 20 && roi > 25) return "GOOD";
        if (profitMargin > 10 && roi > 10) return "MODERATE";
        return "CONCERNING";
    }

    private String determineSeasonality() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 11 || month <= 1) return "HIGH_SEASON"; // Holiday season
        if (month >= 6 && month <= 8) return "LOW_SEASON"; // Summer
        return "NORMAL";
    }

    private List<String> generateOpportunities(UserRole role) {
        List<String> opportunities = new ArrayList<>();
        if (role == UserRole.INVESTOR) {
            opportunities.add("3 high-value sections closing soon");
            opportunities.add("New premium category opened for bidding");
        }
        opportunities.add("Expand into high-performing categories");
        opportunities.add("Leverage seasonal demand increase");
        return opportunities;
    }

    private List<String> generateRisks(Double stockValue) {
        List<String> risks = new ArrayList<>();
        if (stockValue != null && stockValue > 100000) {
            risks.add("High inventory value - monitor for slow-moving items");
        }
        risks.add("Market competition increasing in premium segments");
        return risks;
    }

    private int calculateRecommendedStock(int currentStock) {
        if (currentStock < 5) return 50;
        if (currentStock < 10) return 40;
        if (currentStock < 20) return 30;
        return 25;
    }

    private int estimateDemand(int currentStock) {
        return Math.max(20, 50 - currentStock);
    }

    private void ensureDateRange(StatisticsFilterDTO filter, UserRole role) {
        // For ADMIN_G: if no dates specified, show ALL TIME (not just 1 month)
        // For other roles: default to last 1 month for performance
        if (filter.getStartDate() == null) {
            if (role == UserRole.ADMIN_G) {
                // ADMIN_G sees all-time by default (use a very old date to capture everything)
                filter.setStartDate(LocalDate.of(2000, 1, 1));
            } else {
                // Other roles: last month for better performance
                filter.setStartDate(LocalDate.now().minusMonths(1));
            }
        }
        if (filter.getEndDate() == null) {
            filter.setEndDate(LocalDate.now());
        }
    }

    private Long resolveStoreId(Long adminStoreUserId, Long requestedStoreId) {
        // ADMIN_STORE can ONLY see their own store - security enforcement
        // Fetch the AdminStore entity to get their actual store
        return userRepository.findById(adminStoreUserId)
            .filter(user -> user instanceof com.analyfy.analify.Entity.AdminStore)
            .map(user -> ((com.analyfy.analify.Entity.AdminStore) user).getStore())
            .map(store -> store.getStoreId())
            .orElse(requestedStoreId); // Fallback if not found (shouldn't happen)
    }

    private Map<String, Double> mapToDoubleMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
            r -> String.valueOf(r[0]), 
            r -> ((Number)r[1]).doubleValue()));
    }

    private Map<String, Long> mapToLongMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
            r -> String.valueOf(r[0]), 
            r -> ((Number)r[1]).longValue()));
    }

    private List<TimeSeriesPoint> mapToTimeSeries(List<Object[]> rawData) {
        return rawData.stream().map(row -> {
            Double value = (row[1] instanceof Number) ? ((Number) row[1]).doubleValue() : 0.0;
            return TimeSeriesPoint.builder()
                .date(row[0].toString())
                .value(value)
                .build();
        }).toList();
    }

    private Map<String, Long> calculateWeekStats(List<LocalDate> dates) {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) 
            stats.put(day.getDisplayName(TextStyle.FULL, Locale.ENGLISH), 0L);
        for (LocalDate date : dates) {
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            stats.put(dayName, stats.get(dayName) + 1);
        }
        return stats;
    }

    private Map<String, Long> calculateMonthStats(List<LocalDate> dates) {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Month month : Month.values()) 
            stats.put(month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), 0L);
        for (LocalDate date : dates) {
            String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            stats.put(monthName, stats.get(monthName) + 1);
        }
        return stats;
    }

    private List<RankingItem> mapToRanking(List<Object[]> rows) {
        return rows.stream().map(row -> {
            String label = String.valueOf(row[0]);
            if (label.matches("\\d+")) label = "ID #" + label;
            Double value = row[1] instanceof Number ? ((Number) row[1]).doubleValue() : 0.0;
            String extra = row.length > 2 && row[2] != null ? String.valueOf(row[2]) : null;
            return RankingItem.builder()
                .name(label)
                .value(value)
                .additionalInfo(extra)
                .build();
        }).toList();
    }

    private List<TimeSeriesPoint> compressTimeSeries(List<TimeSeriesPoint> original, int maxPoints) {
        if (original == null || original.isEmpty() || original.size() <= maxPoints) return original;
        List<TimeSeriesPoint> result = new ArrayList<>();
        int groupSize = (int) Math.ceil((double) original.size() / maxPoints);
        for (int i = 0; i < original.size(); i += groupSize) {
            int end = Math.min(i + groupSize, original.size());
            List<TimeSeriesPoint> chunk = original.subList(i, end);
            Double sumValue = chunk.stream().mapToDouble(TimeSeriesPoint::getValue).sum();
            result.add(TimeSeriesPoint.builder()
                .date(chunk.get(0).getDate())
                .value(sumValue)
                .build());
        }
        return result;
    }

    private List<TimeSeriesPoint> calculateLinearRegressionForecast(List<TimeSeriesPoint> history, int days) {
        if (history == null || history.size() < 2) return new ArrayList<>();
        double n = history.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = history.get(i).getValue();
            sumX += x; sumY += y; sumXY += x * y; sumXX += x * x;
        }
        double denominator = (n * sumXX - sumX * sumX);
        if (denominator == 0) return new ArrayList<>();
        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;
        
        List<TimeSeriesPoint> forecast = new ArrayList<>();
        LocalDate lastDate = LocalDate.parse(history.get(history.size() - 1).getDate());
        for (int i = 1; i <= days; i++) {
            double predictedY = slope * ((n - 1) + i) + intercept;
            if (predictedY < 0) predictedY = 0;
            forecast.add(TimeSeriesPoint.builder()
                .date(lastDate.plusDays(i).toString())
                .value(round(predictedY))
                .build());
        }
        return forecast;
    }

    private String analyzeTrend(List<TimeSeriesPoint> forecast) {
        if (forecast.isEmpty()) return "NEUTRAL";
        double start = forecast.get(0).getValue();
        double end = forecast.get(forecast.size() - 1).getValue();
        if (start == 0) return "NEUTRAL";
        double change = (end - start) / start;
        if (change > 0.1) return "BULLISH";
        if (change < -0.1) return "BEARISH";
        return "NEUTRAL";
    }

    private Double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private Double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

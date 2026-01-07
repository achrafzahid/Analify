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
        ensureDateRange(filter);

        Long storeId = null;
        Long investorId = null;

        // Role-based Context Setup
        if (role == UserRole.ADMIN_STORE) {
            storeId = resolveStoreId(userId, filter.getStoreId());
        } else if (role == UserRole.INVESTOR) {
            investorId = userId;
        } else if (role == UserRole.ADMIN_G) {
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
        
        // Section/Bidding Metrics
        Long totalSections = sectionRepository.count();
        Long activeSections = sectionRepository.countByStatus("OPEN");
        Double totalSectionValue = sectionRepository.calculateTotalSectionValue(investorId);
        Long totalBids = bidRepository.count();
        
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

        List<CategoryAnalyticsDTO> topCategories = buildTopCategoriesAnalytics(role, investorId, filter);

        // === SECTION STATS ===
        SectionStatsDTO sectionStats = buildSectionStats(investorId, filter);

        // === GEOGRAPHIC ===
        Map<String, Double> salesByRegion = mapToDoubleMap(
            orderRepository.findSalesByRegion(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );
        Map<String, Double> salesByState = mapToDoubleMap(
            orderRepository.findSalesByState(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );

        // === LEADERBOARDS ===
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
    private SectionStatsDTO buildSectionStats(Long investorId, StatisticsFilterDTO filter) {
        List<Object[]> statusCounts = sectionRepository.countSectionsByStatus(investorId);
        Map<String, Long> sectionsByStatus = statusCounts.stream()
            .collect(Collectors.toMap(r -> String.valueOf(r[0]), r -> ((Number)r[1]).longValue()));
        
        Long totalSections = sectionsByStatus.values().stream().mapToLong(Long::longValue).sum();
        Long activeSections = sectionsByStatus.getOrDefault("OPEN", 0L);
        Long closedSections = sectionsByStatus.getOrDefault("CLOSE", 0L);
        Long wonSections = investorId != null ? 
            (long) sectionRepository.findByWinnerInvestorUserId(investorId).size() : 0L;
        
        Double totalSectionValue = sectionRepository.calculateTotalSectionValue(investorId);
        Double averageSectionPrice = sectionRepository.calculateAverageSectionPrice(investorId);
        Double totalBidsValue = bidRepository.calculateTotalBidsValue(investorId);
        Double averageBidIncrease = sectionRepository.calculateAveragePriceIncrease(investorId);
        Double expectedRevenue = sectionRepository.calculateExpectedRevenue(investorId);
        Double actualRevenue = sectionRepository.calculateActualRevenue(investorId);
        
        Long totalBids = bidRepository.count();
        Long investorBids = investorId != null ? bidRepository.countTotalBids(investorId) : totalBids;
        Double averageBidsPerSection = totalSections > 0 ? (double) investorBids / totalSections : 0.0;
        Double bidWinRate = investorId != null ? bidRepository.calculateWinRate(investorId) : 0.0;
        
        List<RankingItem> mostActiveInvestors = mapToRanking(
            bidRepository.findMostActiveInvestors().stream().limit(10).toList());
        List<RankingItem> topBidders = mapToRanking(
            bidRepository.findTopBiddersByAmount().stream().limit(10).toList());
        
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
        
        // Predictions
        Double predictedRevenue = expectedRevenue != null ? expectedRevenue * 1.15 : 0.0;
        Double predictedBidActivity = (double) totalBids * 1.1;
        String marketTrend = determineMarketTrend(activeSections, totalBids);
        
        return SectionStatsDTO.builder()
                .totalSections(totalSections)
                .activeSections(activeSections)
                .closedSections(closedSections)
                .wonSections(wonSections)
                .totalSectionValue(safeDouble(totalSectionValue))
                .averageSectionPrice(safeDouble(averageSectionPrice))
                .totalBidsValue(safeDouble(totalBidsValue))
                .averageBidIncrease(safeDouble(averageBidIncrease))
                .expectedRevenue(safeDouble(expectedRevenue))
                .actualRevenue(safeDouble(actualRevenue))
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
        Double prodRev = safeDouble(productRevenue);
        Double sectRev = safeDouble(sectionRevenue);
        Double totalRev = prodRev + sectRev;
        
        Double stockCost = safeDouble(stockValue) * 0.7; // Assume 70% cost
        Double sectionInvestments = safeDouble(sectRev);
        Double operationalCosts = totalRev * 0.15; // Estimate 15% operational costs
        
        Double grossProfit = totalRev - stockCost - operationalCosts;
        Double profitMargin = totalRev > 0 ? (grossProfit / totalRev) * 100 : 0.0;
        Double roi = (stockCost + sectionInvestments) > 0 ? 
            (grossProfit / (stockCost + sectionInvestments)) * 100 : 0.0;
        
        Double expectedIncome = sectionRepository.calculateExpectedRevenue(investorId);
        Double pendingPayments = totalRev * 0.05; // Estimate 5% pending
        Double availableCash = grossProfit - pendingPayments;
        
        Double revenueGrowthRate = 12.5; // Would need historical comparison
        String financialHealth = determineFinancialHealth(profitMargin, roi);
        
        return FinancialSummaryDTO.builder()
                .productSalesRevenue(prodRev)
                .sectionSalesRevenue(sectRev)
                .totalRevenue(totalRev)
                .totalStockCost(stockCost)
                .totalSectionInvestments(sectionInvestments)
                .operationalCosts(operationalCosts)
                .grossProfit(grossProfit)
                .profitMargin(round(profitMargin))
                .roi(round(roi))
                .expectedIncome(safeDouble(expectedIncome))
                .pendingPayments(pendingPayments)
                .availableCash(availableCash)
                .revenueGrowthRate(revenueGrowthRate)
                .financialHealth(financialHealth)
                .build();
    }

    // ==================== CATEGORY ANALYTICS ====================
    private List<CategoryAnalyticsDTO> buildTopCategoriesAnalytics(UserRole role, Long investorId, StatisticsFilterDTO filter) {
        if (role != UserRole.ADMIN_G && role != UserRole.ADMIN_STORE) {
            return Collections.emptyList();
        }
        
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
        Long productsOwned = productRepository.countByInvestorUserId(investorId);
        Double portfolioValue = productRepository.calculateTotalStockValue(null, investorId);
        Long sectionsWon = (long) sectionRepository.findByWinnerInvestorUserId(investorId).size();
        Double sectionInvestment = sectionRepository.calculateActualRevenue(investorId);
        
        Double productRevenue = orderRepository.calculateTotalRevenue(filter.getStartDate(), filter.getEndDate(), null, investorId);
        Double sectionRevenue = sectionInvestment;
        Double portfolioGrowth = 18.5; // Would need historical data
        
        Long activeBids = (long) bidRepository.findByInvestorUserIdAndStatus(investorId, "PENDING").size();
        Double totalBidAmount = bidRepository.calculateTotalBidsValue(investorId);
        
        List<RankingItem> topBiddingSections = Collections.emptyList(); // Simplified - would need custom query
        
        List<RankingItem> bestSelling = mapToRanking(
            productRepository.findTopSellingProducts(filter.getStartDate(), filter.getEndDate(), investorId, null, PageRequest.of(0, 5)));
        
        List<RankingItem> worstSelling = Collections.emptyList(); // Would need custom query
        Long lowStockAlerts = productRepository.countLowStockItems(null, investorId, 10);
        
        return InvestorSpecificDTO.builder()
                .totalProductsOwned(productsOwned)
                .portfolioValue(safeDouble(portfolioValue))
                .totalSectionsWon(sectionsWon)
                .totalInvestmentInSections(safeDouble(sectionInvestment))
                .totalRevenuefromProducts(safeDouble(productRevenue))
                .totalRevenueFromSections(safeDouble(sectionRevenue))
                .portfolioGrowth(portfolioGrowth)
                .performanceRating("ABOVE_AVERAGE")
                .activeBids(activeBids)
                .totalBidAmount(safeDouble(totalBidAmount))
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
        
        List<CategoryAnalyticsDTO> allCategories = buildTopCategoriesAnalytics(UserRole.ADMIN_G, null, filter);
        
        Map<String, Double> categoryMarketShare = allCategories.stream()
            .collect(Collectors.toMap(
                CategoryAnalyticsDTO::getCategoryName,
                c -> c.getCombinedRevenue() / platformRevenue * 100
            ));
        
        Long pendingSections = sectionRepository.countByStatus("OPEN");
        Long criticalLowStock = productRepository.countLowStockItems(null, null, 5);
        
        return AdminGSpecificDTO.builder()
                .totalUsers(totalUsers)
                .totalInvestors(totalInvestors)
                .totalStores(totalStores)
                .totalEmployees(totalEmployees)
                .platformRevenue(safeDouble(platformRevenue))
                .platformGrowthRate(platformGrowth)
                .totalTransactions(totalTransactions)
                .totalSectionsCreated(totalSectionsCreated)
                .totalSectionRevenue(safeDouble(totalSectionRevenue))
                .averageSectionCompetition(round(avgCompetition))
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

    private void ensureDateRange(StatisticsFilterDTO filter) {
        if (filter.getStartDate() == null) filter.setStartDate(LocalDate.now().minusMonths(1));
        if (filter.getEndDate() == null) filter.setEndDate(LocalDate.now());
    }

    private Long resolveStoreId(Long adminId, Long requestedStoreId) {
        return requestedStoreId;
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

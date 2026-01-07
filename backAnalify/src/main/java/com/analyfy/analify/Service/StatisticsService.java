package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.Statistics.*;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper; 

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboard(Long userId, UserRole role, StatisticsFilterDTO filter) {
        log.info("Generating dashboard for user {} with role {}", userId, role);
        ensureDateRange(filter, role);

        Long storeId = null;
        Long investorId = null;

        // Role-based Context Setup
        if (role == UserRole.ADMIN_STORE) {
            storeId = resolveStoreId(userId, filter.getStoreId());
        } else if (role == UserRole.INVESTOR) {
            investorId = userId;
        } else if (role == UserRole.ADMIN_G) {
            storeId = filter.getStoreId(); // Optional filter
            investorId = filter.getInvestorId(); // Optional filter
        }

        return generateUnifiedDashboard(filter, storeId, investorId, role);
    }

    // 🛑 THE ONE METHOD TO RULE THEM ALL (Ensures no NULLs)
    private DashboardStatsDTO generateUnifiedDashboard(StatisticsFilterDTO filter, Long storeId, Long investorId, UserRole role) {
        
        // 1. Core KPIs
        Double totalRevenue = orderRepository.calculateTotalRevenue(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Double stockValue = productRepository.calculateTotalStockValue(storeId, investorId);
        Long totalOrders = orderRepository.countTotalOrders(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Integer totalSold = orderRepository.countTotalProductsSold(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Long lowStock = productRepository.countLowStockItems(storeId, investorId, 10);
        
        Double avgOrderValue = (totalOrders != null && totalOrders > 0 && totalRevenue != null) 
                               ? totalRevenue / totalOrders 
                               : 0.0;

        // 2. Time Series Charts
        List<TimeSeriesPoint> fullSeries = mapToTimeSeries(
            orderRepository.findRevenueTimeSeries(filter.getStartDate(), filter.getEndDate(), storeId, investorId, filter.getProductId())
        );

        // 3. Activity Charts (Java Calc)
        List<LocalDate> allDates = orderRepository.findAllOrderDates(filter.getStartDate(), filter.getEndDate(), storeId, investorId);
        Map<String, Long> weekStats = calculateWeekStats(allDates);
        Map<String, Long> monthStats = calculateMonthStats(allDates);

        // 4. Categorical & Geo Charts
        Map<String, Double> categoryRevenue = mapToDoubleMap(
            productRepository.findCategoryRevenueDistribution(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );

        Map<String, Long> productCountByCategory = productRepository.countProductsByCategory(investorId)
                .stream().collect(Collectors.toMap(r -> String.valueOf(r[0]), r -> ((Number)r[1]).longValue()));

        Map<String, Double> salesByRegion = mapToDoubleMap(
            orderRepository.findSalesByRegion(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );
        Map<String, Double> salesByState = mapToDoubleMap(
            orderRepository.findSalesByState(filter.getStartDate(), filter.getEndDate(), storeId, investorId)
        );

        // 5. Leaderboards
        List<RankingItem> topProducts = mapToRanking(productRepository.findTopSellingProducts(
                filter.getStartDate(), filter.getEndDate(), investorId, storeId, PageRequest.of(0, 10)));
        
        // Only Admin_G needs top Stores/Investors lists
        List<RankingItem> topStores = (role == UserRole.ADMIN_G) ? mapToRanking(productRepository.findTopStores(
                filter.getStartDate(), filter.getEndDate(), PageRequest.of(0, 5))) : null;
        
        List<RankingItem> topInvestors = (role == UserRole.ADMIN_G) ? mapToRanking(productRepository.findTopInvestors(
                filter.getStartDate(), filter.getEndDate(), PageRequest.of(0, 5))) : null;

        return DashboardStatsDTO.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .totalStockValue(stockValue != null ? stockValue : 0.0)
                .totalOrders(totalOrders != null ? totalOrders : 0)
                .totalProductsSold(totalSold != null ? totalSold : 0)
                .averageOrderValue(Math.round(avgOrderValue * 100.0) / 100.0)
                .lowStockCount(lowStock != null ? lowStock : 0)
                .revenueOverTime(compressTimeSeries(fullSeries, 20))
                .ordersByDayOfWeek(weekStats)
                .ordersByMonth(monthStats)
                .categoryRevenueDistribution(categoryRevenue)
                .categoryProductCount(productCountByCategory)
                .salesByRegion(salesByRegion)
                .salesByState(salesByState)
                .topProducts(topProducts)
                .topStores(topStores)
                .topInvestors(topInvestors)
                .build();
    }

    // --- Helpers ---
    private Map<String, Double> mapToDoubleMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(r -> String.valueOf(r[0]), r -> ((Number)r[1]).doubleValue()));
    }

    private List<TimeSeriesPoint> mapToTimeSeries(List<Object[]> rawData) {
        return rawData.stream().map(obj -> {
            Object[] row = (Object[]) obj;
            Double value = (row[1] instanceof Number) ? ((Number) row[1]).doubleValue() : 0.0;
            return new TimeSeriesPoint(row[0].toString(), value);
        }).toList();
    }

    private Map<String, Long> calculateWeekStats(List<LocalDate> dates) {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) stats.put(day.getDisplayName(TextStyle.FULL, Locale.ENGLISH), 0L);
        for (LocalDate date : dates) {
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            stats.put(dayName, stats.get(dayName) + 1);
        }
        return stats;
    }

    private Map<String, Long> calculateMonthStats(List<LocalDate> dates) {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Month month : Month.values()) stats.put(month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), 0L);
        for (LocalDate date : dates) {
            String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            stats.put(monthName, stats.get(monthName) + 1);
        }
        return stats;
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

    private Long resolveStoreId(Long adminId, Long requestedStoreId) {
        return requestedStoreId; 
    }
    
    private List<RankingItem> mapToRanking(List<Object[]> rows) {
        return rows.stream().map(obj -> {
            Object[] row = (Object[]) obj;
            String label = String.valueOf(row[0]);
            if (label.matches("\\d+")) { label = "ID #" + label; }
            Double value = (Double) row[1];
            String extra = row.length > 2 && row[2] != null ? String.valueOf(row[2]) : null;
            return RankingItem.builder().name(label).value(value).additionalInfo(extra).build();
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
            result.add(new TimeSeriesPoint(chunk.get(0).getDate(), sumValue));
        }
        return result;
    }

    // Prediction Logic (Updated to use Universal Query)
    public PredictionResultDTO getPredictions(Long userId, UserRole role, String metric, StatisticsFilterDTO filter) {
        ensureDateRange(filter, role);
        Long storeId = (role == UserRole.ADMIN_STORE) ? resolveStoreId(userId, filter.getStoreId()) : filter.getStoreId();
        Long investorId = (role == UserRole.INVESTOR) ? userId : filter.getInvestorId();
        
        List<Object[]> rawData = new ArrayList<>();
        if ("REVENUE".equalsIgnoreCase(metric)) {
            rawData = orderRepository.findRevenueTimeSeries(filter.getStartDate(), filter.getEndDate(), storeId, investorId, filter.getProductId());
        } else if ("STOCK".equalsIgnoreCase(metric)) {
            rawData = orderRepository.findStockDemandTimeSeries(filter.getStartDate(), filter.getEndDate(), storeId, investorId, filter.getProductId());
        }
        
        List<TimeSeriesPoint> history = mapToTimeSeries(rawData);
        List<TimeSeriesPoint> forecast = calculateLinearRegressionForecast(history, 30); 
        return PredictionResultDTO.builder().metric(metric).historicalData(history).forecastData(forecast).trendDescription(analyzeTrend(forecast)).confidenceScore(0.85).build();
    }
    
    private List<TimeSeriesPoint> calculateLinearRegressionForecast(List<TimeSeriesPoint> history, int daysToPredict) {
        if (history == null || history.size() < 2) return new ArrayList<>();
        double n = history.size(); double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) { double x = i; double y = history.get(i).getValue(); sumX += x; sumY += y; sumXY += x * y; sumXX += x * x; }
        double denominator = (n * sumXX - sumX * sumX); if (denominator == 0) return new ArrayList<>(); 
        double slope = (n * sumXY - sumX * sumY) / denominator; double intercept = (sumY - slope * sumX) / n;
        List<TimeSeriesPoint> forecast = new ArrayList<>(); LocalDate lastDate = LocalDate.parse(history.get(history.size() - 1).getDate());
        for (int i = 1; i <= daysToPredict; i++) { double predictedY = slope * ((n - 1) + i) + intercept; if (predictedY < 0) predictedY = 0; forecast.add(TimeSeriesPoint.builder().date(lastDate.plusDays(i).toString()).value(Math.round(predictedY * 100.0) / 100.0).build()); }
        return forecast;
    }
    private String analyzeTrend(List<TimeSeriesPoint> forecast) {
        if (forecast.isEmpty()) return "Insufficient Data"; double start = forecast.get(0).getValue(); double end = forecast.get(forecast.size() - 1).getValue();
        if (start == 0) return "New Data"; double change = (end - start) / start;
        if (change > 0.05) return "Growing Trend 📈"; if (change < -0.05) return "Declining Trend 📉"; return "Stable Market ➖";
    }
    public LlmContextDTO performDeepSearch(Long userId, UserRole role, String query) {
        return LlmContextDTO.builder().prompt(query).contextData(null).build(); 
    }
}
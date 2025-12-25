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

import java.time.LocalDate;
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
        ensureDateRange(filter);

        return switch (role) {
            case INVESTOR -> generateInvestorDashboard(userId, filter);
            case ADMIN_STORE -> generateStoreDashboard(userId, filter);
            case ADMIN_G -> generateGlobalDashboard(filter);
            default -> throw new IllegalArgumentException("Role not supported for analytics: " + role);
        };
    }

    @Transactional(readOnly = true)
    public PredictionResultDTO getPredictions(Long userId, UserRole role, String metric, StatisticsFilterDTO filter) {
        ensureDateRange(filter);
        List<TimeSeriesPoint> history = fetchHistoryForRole(userId, role, metric, filter);
        List<TimeSeriesPoint> forecast = calculateLinearRegressionForecast(history, 30); 

        return PredictionResultDTO.builder()
                .metric(metric)
                .historicalData(history)
                .forecastData(forecast)
                .trendDescription(analyzeTrend(forecast))
                .confidenceScore(0.85)
                .build();
    }

    @Transactional(readOnly = true)
    public LlmContextDTO performDeepSearch(Long userId, UserRole role, String naturalLanguageQuery) {
        StatisticsFilterDTO broadFilter = StatisticsFilterDTO.builder()
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now())
                .build();
        
        DashboardStatsDTO contextStats = getDashboard(userId, role, broadFilter);
        
        String dataJson = "{}";
        try {
            dataJson = objectMapper.writeValueAsString(contextStats);
        } catch (Exception e) {
            log.error("Failed to serialize context", e);
        }

        String systemInstruction = String.format(
            "You are an AI data analyst for the role %s. " +
            "Analyze the provided JSON data to answer the query: '%s'. " +
            "Focus on trends, anomalies, and actionable insights.", 
            role, naturalLanguageQuery
        );

        return LlmContextDTO.builder()
                .prompt(naturalLanguageQuery)
                .contextData(contextStats)
                .suggestedSystemInstruction(systemInstruction)
                .build();
    }

    // --- Private Logic Handlers ---

    private DashboardStatsDTO generateGlobalDashboard(StatisticsFilterDTO filter) {
        Double totalRevenue = orderRepository.calculateTotalRevenue(filter.getStartDate(), filter.getEndDate(), null);
        Double stockValue = productRepository.calculateTotalStockValue(null);
        Long totalOrders = orderRepository.countTotalOrders(filter.getStartDate(), filter.getEndDate(), null);
        Integer totalSold = orderRepository.countTotalProductsSold(filter.getStartDate(), filter.getEndDate(), null);
        Double avgOrderValue = (totalOrders != null && totalOrders > 0 && totalRevenue != null) ? totalRevenue / totalOrders : 0.0;

        // 🛑 Correct Call: 4 Arguments (start, end, storeId=null, productId)
        List<Object[]> rawSeries = orderRepository.findRevenueTimeSeries(
            filter.getStartDate(), filter.getEndDate(), null, filter.getProductId());
            
        List<TimeSeriesPoint> fullSeries = rawSeries.stream()
                .map(obj -> {
                    Object[] row = (Object[]) obj;
                    return new TimeSeriesPoint(row[0].toString(), (Double) row[1]);
                })
                .toList();

        List<RankingItem> topProducts = mapToRanking(productRepository.findTopSellingProducts(
                filter.getStartDate(), filter.getEndDate(), null, null, PageRequest.of(0, 10)));
        
        List<RankingItem> topStores = mapToRanking(productRepository.findTopStores(
                filter.getStartDate(), filter.getEndDate(), PageRequest.of(0, 5)));

        List<RankingItem> topInvestors = mapToRanking(productRepository.findTopInvestors(
                filter.getStartDate(), filter.getEndDate(), PageRequest.of(0, 5)));

        Map<String, Double> categoryDist = productRepository.findCategoryDistribution(filter.getStartDate(), filter.getEndDate(), null)
                .stream().map(obj -> (Object[]) obj)
                .collect(Collectors.toMap(r -> (String)r[0], r -> (Double)r[1]));

        return DashboardStatsDTO.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .totalStockValue(stockValue != null ? stockValue : 0.0)
                .totalOrders(totalOrders != null ? totalOrders : 0)
                .totalProductsSold(totalSold != null ? totalSold : 0)
                .averageOrderValue(Math.round(avgOrderValue * 100.0) / 100.0)
                .revenueOverTime(compressTimeSeries(fullSeries, 20))
                .topProducts(topProducts)
                .topStores(topStores)
                .topInvestors(topInvestors)
                .categoryDistribution(categoryDist)
                .build();
    }

    private DashboardStatsDTO generateStoreDashboard(Long adminId, StatisticsFilterDTO filter) {
        Long storeId = resolveStoreId(adminId, filter.getStoreId());

        Double revenue = orderRepository.calculateTotalRevenue(filter.getStartDate(), filter.getEndDate(), storeId);
        Double stockValue = productRepository.calculateTotalStockValue(storeId);
        Long totalOrders = orderRepository.countTotalOrders(filter.getStartDate(), filter.getEndDate(), storeId);
        Integer totalSold = orderRepository.countTotalProductsSold(filter.getStartDate(), filter.getEndDate(), storeId);
        Double avgOrderValue = (totalOrders != null && totalOrders > 0 && revenue != null) ? revenue / totalOrders : 0.0;

        // 🛑 Correct Call: 4 Arguments (start, end, storeId, productId)
        List<Object[]> rawSeries = orderRepository.findRevenueTimeSeries(
            filter.getStartDate(), filter.getEndDate(), storeId, filter.getProductId());
            
        List<TimeSeriesPoint> fullSeries = rawSeries.stream()
                .map(obj -> {
                    Object[] row = (Object[]) obj;
                    return new TimeSeriesPoint(row[0].toString(), (Double) row[1]);
                })
                .toList();

        List<RankingItem> topProducts = mapToRanking(productRepository.findTopSellingProducts(
                filter.getStartDate(), filter.getEndDate(), null, storeId, PageRequest.of(0, 10)));

        Map<String, Double> categoryDist = productRepository.findCategoryDistribution(filter.getStartDate(), filter.getEndDate(), storeId)
                .stream().map(obj -> (Object[]) obj)
                .collect(Collectors.toMap(r -> (String)r[0], r -> (Double)r[1]));

        return DashboardStatsDTO.builder()
                .totalRevenue(revenue != null ? revenue : 0.0)
                .totalStockValue(stockValue != null ? stockValue : 0.0)
                .totalOrders(totalOrders != null ? totalOrders : 0)
                .totalProductsSold(totalSold != null ? totalSold : 0)
                .averageOrderValue(Math.round(avgOrderValue * 100.0) / 100.0)
                .revenueOverTime(compressTimeSeries(fullSeries, 20))
                .topProducts(topProducts)
                .categoryDistribution(categoryDist)
                .build();
    }

    private DashboardStatsDTO generateInvestorDashboard(Long investorId, StatisticsFilterDTO filter) {
        List<Object[]> rawProducts = productRepository.findTopSellingProducts(
                filter.getStartDate(), filter.getEndDate(), investorId, null, PageRequest.of(0, 10));

        List<RankingItem> topProducts = mapToRanking(rawProducts);
        Double revenue = topProducts.stream().mapToDouble(RankingItem::getValue).sum();

        return DashboardStatsDTO.builder()
                .totalRevenue(revenue)
                .topProducts(topProducts)
                .revenueOverTime(Collections.emptyList()) 
                .build();
    }

    // --- Helpers ---

    private void ensureDateRange(StatisticsFilterDTO filter) {
        if (filter.getStartDate() == null) filter.setStartDate(LocalDate.now().minusMonths(1));
        if (filter.getEndDate() == null) filter.setEndDate(LocalDate.now());
    }

    private Long resolveStoreId(Long adminId, Long requestedStoreId) {
        return requestedStoreId; 
    }

    private List<TimeSeriesPoint> fetchHistoryForRole(Long userId, UserRole role, String metric, StatisticsFilterDTO filter) {
        // 🛑 FIXED SCOPE: Define storeId here so it is visible in both if/else blocks
        Long storeId = (role == UserRole.ADMIN_STORE) ? resolveStoreId(userId, filter.getStoreId()) : filter.getStoreId();
        
        List<Object[]> rawData = new ArrayList<>();

        if ("REVENUE".equalsIgnoreCase(metric)) {
             // 🛑 Correct Call: 4 Arguments
             rawData = orderRepository.findRevenueTimeSeries(
                 filter.getStartDate(), 
                 filter.getEndDate(), 
                 storeId, 
                 filter.getProductId()
             );
        } 
        else if ("STOCK".equalsIgnoreCase(metric)) {
             if (role == UserRole.INVESTOR) {
                rawData = orderRepository.findInvestorStockDemand(
                    filter.getStartDate(), filter.getEndDate(), userId, filter.getProductId());
            } else {
                rawData = orderRepository.findStockDemandTimeSeries(
                    filter.getStartDate(), filter.getEndDate(), storeId);
            }
        }

        return rawData.stream()
             .map(obj -> {
                Object[] row = (Object[]) obj;
                Double value = (row[1] instanceof Number) ? ((Number) row[1]).doubleValue() : 0.0;
                return new TimeSeriesPoint(row[0].toString(), value);
             })
             .toList();
    }

    private List<RankingItem> mapToRanking(List<Object[]> rows) {
        return rows.stream().map(obj -> {
            Object[] row = (Object[]) obj;
            
            // Crash proof safe string conversion
            String label = String.valueOf(row[0]); 
            if (label.matches("\\d+")) { label = "ID #" + label; }

            Double value = (Double) row[1];
            String extra = row.length > 2 && row[2] != null ? String.valueOf(row[2]) : null;

            return RankingItem.builder()
                .name(label)
                .value(value)
                .additionalInfo(extra)
                .build();
        }).toList();
    }

    private List<TimeSeriesPoint> calculateLinearRegressionForecast(List<TimeSeriesPoint> history, int daysToPredict) {
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

        for (int i = 1; i <= daysToPredict; i++) {
            double predictedY = slope * ((n - 1) + i) + intercept;
            if (predictedY < 0) predictedY = 0;

            forecast.add(TimeSeriesPoint.builder()
                    .date(lastDate.plusDays(i).toString())
                    .value(Math.round(predictedY * 100.0) / 100.0)
                    .build());
        }
        return forecast;
    }

    private String analyzeTrend(List<TimeSeriesPoint> forecast) {
        if (forecast.isEmpty()) return "Insufficient Data";
        double start = forecast.get(0).getValue();
        double end = forecast.get(forecast.size() - 1).getValue();
        
        if (start == 0) return "New Data";
        
        double change = (end - start) / start;
        if (change > 0.05) return "Growing Trend 📈";
        if (change < -0.05) return "Declining Trend 📉";
        return "Stable Market ➖";
    }

    private List<TimeSeriesPoint> compressTimeSeries(List<TimeSeriesPoint> original, int maxPoints) {
        if (original == null || original.isEmpty() || original.size() <= maxPoints) {
            return original;
        }

        List<TimeSeriesPoint> result = new ArrayList<>();
        int groupSize = (int) Math.ceil((double) original.size() / maxPoints);

        for (int i = 0; i < original.size(); i += groupSize) {
            int end = Math.min(i + groupSize, original.size());
            List<TimeSeriesPoint> chunk = original.subList(i, end);

            Double sumValue = chunk.stream().mapToDouble(TimeSeriesPoint::getValue).sum();
            String label = chunk.get(0).getDate();

            result.add(new TimeSeriesPoint(label, sumValue));
        }
        return result;
    }
}
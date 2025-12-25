package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.Statistics.*; // ⚠️ Check if your folder is named 'Dto' or 'DTO'
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Main Dashboard Endpoint.
     * Routes to Global, Store, or Investor dashboard based on the Role header.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboard(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingRole,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long investorId) {

        // Build the filter object from request params
        StatisticsFilterDTO filter = StatisticsFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .storeId(storeId)
                .investorId(investorId)
                .build();

        return ResponseEntity.ok(statisticsService.getDashboard(actingUserId, actingRole, filter));
    }

    /**
     * Predictions Endpoint.
     * Returns forecast data for specific metrics (REVENUE, STOCK, etc.).
     */
    @GetMapping("/predictions")
    public ResponseEntity<PredictionResultDTO> getPredictions(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingRole,
            @RequestParam String metric,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long productId, // 🆕 Added
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, // 🆕 Added
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) { // 🆕 Added

        // Build the filter correctly so the Service sees the productId
        StatisticsFilterDTO filter = StatisticsFilterDTO.builder()
                .storeId(storeId)
                .productId(productId) // 🆕 Pass it to the filter
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return ResponseEntity.ok(statisticsService.getPredictions(actingUserId, actingRole, metric, filter));
    }

    /**
     * Deep Search Endpoint (AI Ready).
     * Returns JSON context for an LLM to answer natural language queries.
     */
    @PostMapping("/deep-search")
    public ResponseEntity<LlmContextDTO> deepSearch(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingRole,
            @RequestBody String naturalLanguageQuery) {

        return ResponseEntity.ok(statisticsService.performDeepSearch(actingUserId, actingRole, naturalLanguageQuery));
    }
}
package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.Statistics.*;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboard(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long investorId,
            @RequestParam(required = false) Long productId) {

        StatisticsFilterDTO filter = StatisticsFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .storeId(storeId)
                .investorId(investorId)
                .productId(productId)
                .build();

        return ResponseEntity.ok(statisticsService.getDashboard(userId, role, filter));
    }

    @GetMapping("/predictions")
    public ResponseEntity<PredictionResultDTO> getPredictions(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @RequestParam String metric,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // For Investors, userId implies investorId, handled in Service.
        StatisticsFilterDTO filter = StatisticsFilterDTO.builder()
                .storeId(storeId)
                .productId(productId)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return ResponseEntity.ok(statisticsService.getPredictions(userId, role, metric, filter));
    }

    @PostMapping("/deep-search")
    public ResponseEntity<LlmContextDTO> deepSearch(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @RequestBody Map<String, String> requestBody) {

        String query = requestBody.get("query");
        return ResponseEntity.ok(statisticsService.performDeepSearch(userId, role, query));
    }
}
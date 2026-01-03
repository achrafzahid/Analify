package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class StatisticsFilterDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long storeId;
    private Long investorId;
    private Long productId;
    private String region;
    private Double minRevenue;
    private Double maxRevenue;
    private String searchQuery;
}
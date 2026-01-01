package com.analyfy.analify.DTO.Bids;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeasonConfigDTO {
    private Integer currentMonth;
    private Integer currentSeason; // 1, 2, 3, 4
    private LocalDate seasonStartDate;
    private LocalDate seasonEndDate;
    private LocalDate biddingOpenDate;
    private LocalDate biddingCloseDate;
    private Boolean isBiddingOpen;
    private Integer daysUntilClose;
}
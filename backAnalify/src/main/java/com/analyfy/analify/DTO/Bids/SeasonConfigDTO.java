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
    private Integer currentPeriod; // 1-12 (mois de l'année)
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private LocalDate biddingOpenDate;
    private LocalDate biddingCloseDate;
    private Boolean isBiddingOpen;
    private Integer daysUntilClose;
}
package com.analyfy.analify.DTO.Statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankingItem {
    private String name;
    private Double value;
    private String additionalInfo;
}
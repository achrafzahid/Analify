package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsightDTO {
    private String type; // OPPORTUNITY, WARNING, INFO, SUCCESS
    private String title;
    private String description;
    private String actionRecommendation;
    private String severity; // HIGH, MEDIUM, LOW
    private String icon; // For frontend
}

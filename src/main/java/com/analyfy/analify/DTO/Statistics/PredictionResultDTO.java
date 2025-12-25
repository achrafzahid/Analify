package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PredictionResultDTO {
    private String metric;
    private List<TimeSeriesPoint> historicalData;
    private List<TimeSeriesPoint> forecastData;
    private String trendDescription;
    private Double confidenceScore;
}
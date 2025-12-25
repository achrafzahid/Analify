package com.analyfy.analify.DTO.Statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LlmContextDTO {
    private String prompt;
    private Object contextData;
    private String suggestedSystemInstruction;
}
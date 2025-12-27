package com.analyfy.analify.DTO;

import lombok.Data;

@Data
public class UpdateProductRequest {
    private String productName;
    private Long subId;
    private Long investorId;
}
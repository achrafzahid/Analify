package com.analyfy.analify.DTO.Bids;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidHistoryDTO {
    private Long bidId;
    private Double amount;
    private LocalDateTime bidTime;
    private String investorName;
    private String status;
    private Boolean isCurrentWinner;
}
package com.lab.reagent.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DiscardDTO {
    private Long inventoryId;
    private BigDecimal quantity;
    private String method; // Disposal method
    private String remark;
}

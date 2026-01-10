package com.lab.reagent.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 试剂信息（用于位置统计）
 */
@Data
public class ReagentInfo {
    
    private Long inventoryId;
    
    private String reagentName;
    
    private String batchNo;
    
    private BigDecimal quantity;
    
    private String unit;
    
    private String status;
}



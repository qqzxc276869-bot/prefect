package com.lab.reagent.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库存信息VO
 */
@Data
public class InventoryVO {
    
    private Long id;
    
    private Long reagentId;
    
    private String reagentName;
    
    private String casNo;
    
    private String categoryName;
    
    private String specification;
    
    private String unit;
    
    private String batchNo;
    
    private BigDecimal quantity;
    
    private BigDecimal warningThreshold;
    
    private LocalDate expiryDate;
    
    private String locationName;
    
    private String supplier;
    
    private String status;
    
    private String dangerLevel;
}








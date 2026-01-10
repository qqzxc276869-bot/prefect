package com.lab.reagent.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 位置统计信息VO
 */
@Data
public class LocationStatisticsVO {
    
    private Long locationId;
    
    private String roomName;
    
    private String cabinetNo;
    
    private String shelfNo;
    
    private String fullLocation;
    
    private String description;
    
    private Integer reagentCount;  // 该位置的试剂种类数
    
    private BigDecimal totalQuantity;  // 总数量
    
    private Integer occupancyRate;  // 占用率（百分比）
    
    private List<ReagentInfo> reagents;  // 该位置的试剂列表
}



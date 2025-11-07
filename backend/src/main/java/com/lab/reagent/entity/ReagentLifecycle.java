package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 试剂生命周期记录实体
 */
@Data
@TableName("reagent_lifecycle")
public class ReagentLifecycle implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long inventoryId;
    
    private Long reagentId;
    
    private String qrCode;
    
    private String rfidTag;
    
    private String physicalStatus;
    
    private String sealIntegrity;
    
    private LocalDateTime openedDate;
    
    private Long openedById;
    
    private String openedByName;
    
    private Integer openedValidityDays;
    
    private LocalDate openedExpiryDate;
    
    private BigDecimal remainingQuantity;
    
    private LocalDateTime lastUsedDate;
    
    private Integer usageCount;
    
    private Long currentLocationId;
    
    private String locationHistory;  // JSON数组
    
    private String conditionNotes;
    
    private String photoPaths;  // JSON数组
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


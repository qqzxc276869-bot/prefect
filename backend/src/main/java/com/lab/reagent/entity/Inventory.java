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
 * 库存实体
 */
@Data
@TableName("inventory")
public class Inventory implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reagentId;
    
    private Long locationId;
    
    private String batchNo;
    
    private BigDecimal quantity;
    
    private BigDecimal warningThreshold;
    
    private LocalDate expiryDate;
    
    private String supplier;
    
    private LocalDate purchaseDate;
    
    private BigDecimal purchasePrice;
    
    private String status;  // NORMAL, LOW, EXPIRING, EXPIRED
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}







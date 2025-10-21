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
 * 入库记录实体
 */
@Data
@TableName("stock_in_record")
public class StockInRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reagentId;
    
    private Long inventoryId;
    
    private String batchNo;
    
    private BigDecimal quantity;
    
    private LocalDate expiryDate;
    
    private String supplier;
    
    private BigDecimal purchasePrice;
    
    private Long operatorId;
    
    private String operatorName;
    
    private String remark;
    
    private LocalDateTime createTime;
}







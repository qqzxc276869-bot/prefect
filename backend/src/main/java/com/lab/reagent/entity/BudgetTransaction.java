package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算使用记录实体
 */
@Data
@TableName("budget_transaction")
public class BudgetTransaction implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long groupId;
    
    private String transactionType;
    
    private BigDecimal amount;
    
    private Long relatedId;
    
    private String relatedType;
    
    private BigDecimal balanceBefore;
    
    private BigDecimal balanceAfter;
    
    private String reagentName;
    
    private BigDecimal quantity;
    
    private String description;
    
    private Long operatorId;
    
    private String operatorName;
    
    private LocalDateTime createTime;
}


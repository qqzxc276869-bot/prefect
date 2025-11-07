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
 * 采购申请实体
 */
@Data
@TableName("procurement_request")
public class ProcurementRequest implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String requestNo;
    
    private Long groupId;
    
    private Long reagentId;
    
    private String reagentName;
    
    private String specification;
    
    private BigDecimal quantity;
    
    private String unit;
    
    private BigDecimal estimatedPrice;
    
    private BigDecimal estimatedTotal;
    
    private String purpose;
    
    private String urgencyLevel;
    
    private Long applicantId;
    
    private String applicantName;
    
    private String status;
    
    private Long piReviewerId;
    
    private String piReviewerName;
    
    private LocalDateTime piReviewTime;
    
    private String piReviewRemark;
    
    private Long adminReviewerId;
    
    private String adminReviewerName;
    
    private LocalDateTime adminReviewTime;
    
    private String adminReviewRemark;
    
    private String supplier;
    
    private BigDecimal actualPrice;
    
    private BigDecimal actualTotal;
    
    private LocalDate orderDate;
    
    private LocalDate expectedArrivalDate;
    
    private LocalDate actualArrivalDate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


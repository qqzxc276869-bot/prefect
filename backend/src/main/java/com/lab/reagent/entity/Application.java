package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领用申请实体
 */
@Data
@TableName("application")
public class Application implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String applicationNo;
    
    private Long reagentId;
    
    private String reagentName;
    
    private BigDecimal quantity;
    
    private String purpose;
    
    private Long applicantId;
    
    private String applicantName;
    
    private String status;  // PENDING, APPROVED, REJECTED, COMPLETED
    
    private Long reviewerId;
    
    private String reviewerName;
    
    private LocalDateTime reviewTime;
    
    private String reviewRemark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}








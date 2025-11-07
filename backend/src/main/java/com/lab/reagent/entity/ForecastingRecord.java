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
 * 智能预测记录实体
 */
@Data
@TableName("forecasting_record")
public class ForecastingRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reagentId;
    
    private String reagentName;
    
    private BigDecimal currentStock;
    
    private LocalDate predictedDepletionDate;
    
    private Integer daysUntilDepletion;
    
    private BigDecimal averageDailyConsumption;
    
    private Integer supplierLeadTime;
    
    private LocalDate recommendedOrderDate;
    
    private BigDecimal recommendedOrderQuantity;
    
    private BigDecimal confidenceLevel;
    
    private String predictionModel;
    
    private String predictionBasis;  // JSON
    
    private String status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


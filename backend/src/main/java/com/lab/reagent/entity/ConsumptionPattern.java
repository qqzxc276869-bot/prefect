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
 * 消耗模式分析实体
 */
@Data
@TableName("consumption_pattern")
public class ConsumptionPattern implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reagentId;
    
    private Long groupId;
    
    private Long userId;
    
    private String timePeriod;
    
    private LocalDate periodStartDate;
    
    private LocalDate periodEndDate;
    
    private BigDecimal totalConsumption;
    
    private Integer consumptionFrequency;
    
    private BigDecimal averageConsumption;
    
    private String consumptionTrend;
    
    private String patternType;
    
    private String analysisResult;  // JSON
    
    private Integer aiGenerated;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


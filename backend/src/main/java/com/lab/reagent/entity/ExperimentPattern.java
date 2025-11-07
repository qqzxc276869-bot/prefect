package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实验模式识别实体
 */
@Data
@TableName("experiment_pattern")
public class ExperimentPattern implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String patternName;
    
    private String patternCode;
    
    private String patternType;
    
    private String reagentBom;  // JSON数组
    
    private String typicalQuantities;  // JSON
    
    private Integer frequencyThreshold;
    
    private BigDecimal confidenceScore;
    
    private String description;
    
    private Integer aiLearned;
    
    private Integer usageCount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


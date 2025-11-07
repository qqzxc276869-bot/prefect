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
 * 实验活动记录实体
 */
@Data
@TableName("experiment_activity")
public class ExperimentActivity implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String activityNo;
    
    private Long patternId;
    
    private String patternName;
    
    private Long userId;
    
    private String userName;
    
    private Long groupId;
    
    private String reagentsUsed;  // JSON数组
    
    private LocalDate activityDate;
    
    private BigDecimal confidenceLevel;
    
    private Integer autoDetected;
    
    private LocalDateTime createTime;
}


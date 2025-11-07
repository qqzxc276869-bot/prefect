package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 存储不兼容规则实体
 */
@Data
@TableName("storage_incompatibility_rules")
public class StorageIncompatibilityRules implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String hazardType1;
    
    private String hazardType2;
    
    private String incompatibilityLevel;
    
    private String description;
    
    private BigDecimal safetyDistance;
    
    private Integer enabled;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


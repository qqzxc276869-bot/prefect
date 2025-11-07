package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 废弃物类别实体
 */
@Data
@TableName("waste_category")
public class WasteCategory implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String categoryName;
    
    private String categoryCode;
    
    private String hazardLevel;
    
    private String unNumber;
    
    private String description;
    
    private String disposalMethod;
    
    private String storageRequirements;
    
    private String labelColor;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


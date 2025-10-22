package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 试剂实体
 */
@Data
@TableName("reagent")
public class Reagent implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String casNo;
    
    private Long categoryId;
    
    private String specification;
    
    private String unit;
    
    private String manufacturer;
    
    private Integer supplierLeadTime; // 供应商到货周期(天)
    
    private String dangerLevel;
    
    private String description;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}








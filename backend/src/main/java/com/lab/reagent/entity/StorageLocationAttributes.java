package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库位属性实体
 */
@Data
@TableName("storage_location_attributes")
public class StorageLocationAttributes implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long locationId;
    
    private Integer isVentilated;
    
    private Integer isExplosionProof;
    
    private String allowedHazardTypes;  // JSON数组
    
    private String forbiddenHazardTypes;  // JSON数组
    
    private String temperatureControl;
    
    private String humidityControl;
    
    private BigDecimal maxCapacity;
    
    private String specialRequirements;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


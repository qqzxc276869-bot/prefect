package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库位坐标实体
 */
@Data
@TableName("storage_location_coordinate")
public class StorageLocationCoordinate implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long locationId;
    
    private Long mapId;
    
    private BigDecimal xCoordinate;
    
    private BigDecimal yCoordinate;
    
    private BigDecimal width;
    
    private BigDecimal height;
    
    private String shape;
    
    private String color;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


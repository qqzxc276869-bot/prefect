package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库位可视化配置实体
 */
@Data
@TableName("storage_location_map")
public class StorageLocationMap implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roomName;
    
    private String mapType;
    
    private String mapImagePath;
    
    private String mapConfig;  // JSON
    
    private String description;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


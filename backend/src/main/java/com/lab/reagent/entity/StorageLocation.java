package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存放位置实体
 */
@Data
@TableName("storage_location")
public class StorageLocation implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roomName;
    
    private String cabinetNo;
    
    private String shelfNo;
    
    private String fullLocation;
    
    private String description;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}








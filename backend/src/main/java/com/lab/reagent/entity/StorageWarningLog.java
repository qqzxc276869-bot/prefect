package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存储警告日志实体
 */
@Data
@TableName("storage_warning_log")
public class StorageWarningLog implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reagentId;
    
    private Long locationId;
    
    private String warningType;
    
    private String warningLevel;
    
    private String warningMessage;
    
    private Long operatorId;
    
    private String operatorName;
    
    private String actionTaken;
    
    private String overrideReason;
    
    private LocalDateTime createTime;
}


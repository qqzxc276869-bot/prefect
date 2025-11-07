package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 扫码操作日志实体
 */
@Data
@TableName("scan_operation_log")
public class ScanOperationLog implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String qrCode;
    
    private String scanType;
    
    private String operationType;
    
    private Long userId;
    
    private String userName;
    
    private String deviceInfo;
    
    private String locationInfo;
    
    private String result;
    
    private String errorMessage;
    
    private LocalDateTime createTime;
}


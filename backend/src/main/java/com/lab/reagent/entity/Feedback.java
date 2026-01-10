package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问题反馈实体
 */
@Data
@TableName("feedback")
public class Feedback implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String userName;
    
    private String feedbackType;  // REAGENT-试剂问题, SYSTEM-系统问题, SUGGESTION-建议
    
    private String title;
    
    private String content;
    
    private String status;  // PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, CLOSED-已关闭
    
    private Long handlerId;
    
    private String handlerName;
    
    private String handleRemark;
    
    private LocalDateTime handleTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}





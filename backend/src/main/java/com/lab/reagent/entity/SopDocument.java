package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SOP 标准操作规程实体
 */
@Data
@TableName("sop_document")
public class SopDocument implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String sopNo;
    
    private String version;
    
    private String category;
    
    private String content;
    
    private String filePath;
    
    private String applicableReagents;  // JSON数组
    
    private String applicableHazardTypes;  // JSON数组
    
    private String keyPoints;  // JSON数组
    
    private LocalDate effectiveDate;
    
    private LocalDate reviewDate;
    
    private String status;
    
    private Long authorId;
    
    private String authorName;
    
    private Long reviewerId;
    
    private String reviewerName;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


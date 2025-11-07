package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SOP 培训与准入记录实体
 */
@Data
@TableName("sop_training_record")
public class SopTrainingRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String userName;
    
    private Long sopId;
    
    private String sopNo;
    
    private String trainingType;
    
    private String testQuestions;  // JSON数组
    
    private String testAnswers;  // JSON数组
    
    private BigDecimal testScore;
    
    private Integer passed;
    
    private LocalDateTime trainingTime;
    
    private LocalDate validUntil;
    
    private LocalDateTime createTime;
}


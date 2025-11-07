package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课题组/项目实体
 */
@Data
@TableName("research_group")
public class ResearchGroup implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String groupName;
    
    private String groupCode;
    
    private Long piId;
    
    private String piName;
    
    private String department;
    
    private BigDecimal totalBudget;
    
    private BigDecimal usedBudget;
    
    private BigDecimal availableBudget;
    
    private Integer budgetYear;
    
    private String status;
    
    private String description;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


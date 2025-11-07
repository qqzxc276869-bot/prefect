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
 * 废弃物登记实体
 */
@Data
@TableName("waste_disposal_record")
public class WasteDisposalRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String disposalNo;
    
    private Long wasteCategoryId;
    
    private String wasteType;
    
    private String sourceType;
    
    private Long sourceReagentId;
    
    private Long sourceInventoryId;
    
    private String mainComponents;
    
    private BigDecimal quantity;
    
    private String unit;
    
    private String containerType;
    
    private Integer containerCount;
    
    private String hazardProperties;  // JSON数组
    
    private String disposalStatus;
    
    private String storageLocation;
    
    private Long applicantId;
    
    private String applicantName;
    
    private Long groupId;
    
    private LocalDate disposalDate;
    
    private LocalDate collectionDate;
    
    private String collectionCompany;
    
    private String collectionPerson;
    
    private String disposalCompany;
    
    private String disposalMethod;
    
    private String disposalCertificateNo;
    
    private String disposalCertificatePath;
    
    private Integer labelPrinted;
    
    private String labelInfo;  // JSON
    
    private String photos;  // JSON数组
    
    private String remark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * GHS/MSDS 结构化数据实体
 */
@Data
@TableName("ghs_msds")
public class GhsMsds implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reagentId;
    
    private String msdsFilePath;
    
    private String ghsPictograms;  // JSON数组
    
    private String hPhrases;  // JSON数组
    
    private String pPhrases;  // JSON数组
    
    private String signalWord;
    
    private String hazardClass;
    
    private String precautionaryMeasures;
    
    private String firstAidMeasures;
    
    private String fireFightingMeasures;
    
    private String accidentalReleaseMeasures;
    
    private String handlingAndStorage;
    
    private String exposureControls;
    
    private String physicalChemicalProperties;  // JSON
    
    private String stabilityReactivity;
    
    private String toxicologicalInformation;
    
    private String ecologicalInformation;
    
    private String disposalConsiderations;
    
    private String transportInformation;
    
    private String regulatoryInformation;
    
    private Integer aiExtracted;
    
    private Integer verified;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


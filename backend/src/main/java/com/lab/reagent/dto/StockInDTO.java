package com.lab.reagent.dto;

import com.lab.reagent.entity.StockInRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockInDTO extends StockInRecord {
    // 新增试剂时需要的字段
    private String reagentName;
    private String casNo;
    private String specification;
    private String unit;
    private String dangerLevel;
    private Long categoryId;
    private String manufacturer;
    private Integer supplierLeadTime;
}

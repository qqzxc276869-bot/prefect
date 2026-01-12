package com.lab.reagent.vo;

import com.lab.reagent.entity.StockInRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入库记录VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockInVO extends StockInRecord {
    private String reagentName;
}

package com.lab.reagent.vo;

import com.lab.reagent.entity.StockOutRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出库记录VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockOutVO extends StockOutRecord {
    private String reagentName;
    private String batchNo;
}

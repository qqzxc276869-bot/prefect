package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库记录实体
 */
@Data
@TableName("stock_out_record")
public class StockOutRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reagentId;
    
    private Long inventoryId;
    
    private Long applicationId;
    
    private BigDecimal quantity;
    
    private Long recipientId;
    
    private String recipientName;
    
    private Long operatorId;
    
    private String operatorName;
    
    private String purpose;
    
    private String remark;
    
    private LocalDateTime createTime;
}







package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 废弃记录实体
 */
@Data
@TableName("waste_record")
public class WasteRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long reagentId;

    private String reagentName;

    private Long inventoryId;

    private BigDecimal quantity;

    private String method;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;
}

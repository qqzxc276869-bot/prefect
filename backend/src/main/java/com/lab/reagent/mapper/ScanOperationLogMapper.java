package com.lab.reagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.reagent.entity.ScanOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 扫码操作日志 Mapper
 */
@Mapper
public interface ScanOperationLogMapper extends BaseMapper<ScanOperationLog> {
}


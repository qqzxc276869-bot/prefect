package com.lab.reagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.reagent.entity.ForecastingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能预测记录 Mapper
 */
@Mapper
public interface ForecastingRecordMapper extends BaseMapper<ForecastingRecord> {
}


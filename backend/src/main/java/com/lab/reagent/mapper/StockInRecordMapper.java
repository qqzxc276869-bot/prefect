package com.lab.reagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.StockInRecord;
import com.lab.reagent.vo.StockInVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 入库记录Mapper
 */
@Mapper
public interface StockInRecordMapper extends BaseMapper<StockInRecord> {

    @Select("SELECT s.*, r.name AS reagent_name " +
            "FROM stock_in_record s " +
            "LEFT JOIN reagent r ON s.reagent_id = r.id " +
            "ORDER BY s.create_time DESC")
    Page<StockInVO> selectPageWithVO(Page<StockInVO> page);
}

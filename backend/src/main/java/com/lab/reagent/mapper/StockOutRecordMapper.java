package com.lab.reagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.StockOutRecord;
import com.lab.reagent.vo.StockOutVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 出库记录Mapper
 */
@Mapper
public interface StockOutRecordMapper extends BaseMapper<StockOutRecord> {

    @Select("SELECT s.*, r.name AS reagent_name, i.batch_no " +
            "FROM stock_out_record s " +
            "LEFT JOIN reagent r ON s.reagent_id = r.id " +
            "LEFT JOIN inventory i ON s.inventory_id = i.id " +
            "ORDER BY s.create_time DESC")
    Page<StockOutVO> selectPageWithVO(Page<StockOutVO> page);
}

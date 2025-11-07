package com.lab.reagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.reagent.entity.BudgetTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算交易记录 Mapper
 */
@Mapper
public interface BudgetTransactionMapper extends BaseMapper<BudgetTransaction> {
}


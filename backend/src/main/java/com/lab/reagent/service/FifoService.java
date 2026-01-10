package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.entity.Reagent;
import com.lab.reagent.mapper.InventoryMapper;
import com.lab.reagent.mapper.ReagentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIFO先进先出Service
 */
@Service
public class FifoService {
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    @Autowired
    private ReagentMapper reagentMapper;
    
    /**
     * 获取FIFO建议
     */
    public Map<String, Object> getFifoSuggestion(Long reagentId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查询该试剂的所有库存批次
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getReagentId, reagentId);
        wrapper.gt(Inventory::getQuantity, 0);
        wrapper.orderByAsc(Inventory::getExpiryDate);
        
        List<Inventory> inventoryList = inventoryMapper.selectList(wrapper);
        
        if (inventoryList.isEmpty()) {
            result.put("hasSuggestion", false);
            result.put("message", "该试剂暂无库存");
            return result;
        }
        
        // 获取最早过期的批次
        Inventory earliestBatch = inventoryList.get(0);
        Reagent reagent = reagentMapper.selectById(reagentId);
        
        result.put("hasSuggestion", true);
        result.put("batchNo", earliestBatch.getBatchNo());
        result.put("inventoryId", earliestBatch.getId());
        result.put("quantity", earliestBatch.getQuantity());
        result.put("expiryDate", earliestBatch.getExpiryDate());
        result.put("reagentName", reagent != null ? reagent.getName() : "");
        
        // 计算距离过期天数
        if (earliestBatch.getExpiryDate() != null) {
            long daysToExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), earliestBatch.getExpiryDate());
            result.put("daysToExpiry", daysToExpiry);
            
            if (daysToExpiry < 0) {
                result.put("message", String.format("根据先进先出原则，建议从批次[%s]出库，该批次已过期%d天", 
                    earliestBatch.getBatchNo(), Math.abs(daysToExpiry)));
            } else if (daysToExpiry < 30) {
                result.put("message", String.format("根据先进先出原则，建议从批次[%s]出库，该批次将在%d天后过期", 
                    earliestBatch.getBatchNo(), daysToExpiry));
            } else {
                result.put("message", String.format("根据先进先出原则，建议从批次[%s]出库，该批次最早过期（%s）", 
                    earliestBatch.getBatchNo(), earliestBatch.getExpiryDate()));
            }
        } else {
            result.put("message", String.format("根据先进先出原则，建议从批次[%s]出库", earliestBatch.getBatchNo()));
        }
        
        return result;
    }
}





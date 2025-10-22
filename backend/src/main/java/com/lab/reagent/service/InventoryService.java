package com.lab.reagent.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.mapper.InventoryMapper;
import com.lab.reagent.vo.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存Service
 */
@Service
public class InventoryService extends ServiceImpl<InventoryMapper, Inventory> {
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    /**
     * 查询库存列表
     */
    public List<InventoryVO> getInventoryList(String name) {
        return inventoryMapper.selectInventoryList(name);
    }
    
    /**
     * 查询预警列表
     */
    public List<InventoryVO> getWarningList() {
        return inventoryMapper.selectWarningList();
    }
    

    
    /**
     * 更新库存状态
     */
    public void updateInventoryStatus(Long id) {
        Inventory inventory = this.getById(id);
        if (inventory == null) {
            return;
        }
        
        String status = "NORMAL";
        
        // 检查库存是否不足
        if (inventory.getQuantity().compareTo(inventory.getWarningThreshold()) <= 0) {
            status = "LOW";
        }
        
        // 检查是否临期或过期
        if (inventory.getExpiryDate() != null) {
            LocalDate now = LocalDate.now();
            LocalDate warningDate = now.plusDays(30); // 提前30天预警
            
            if (inventory.getExpiryDate().isBefore(now)) {
                status = "EXPIRED";
            } else if (inventory.getExpiryDate().isBefore(warningDate)) {
                status = "EXPIRING";
            }
        }
        
        inventory.setStatus(status);
        this.updateById(inventory);
    }
    
    /**
     * 增加库存
     */
    public void increaseStock(Long id, BigDecimal quantity) {
        Inventory inventory = this.getById(id);
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }
        
        inventory.setQuantity(inventory.getQuantity().add(quantity));
        this.updateById(inventory);
        
        // 更新状态
        updateInventoryStatus(id);
    }
    
    /**
     * 减少库存
     */
    public void decreaseStock(Long id, BigDecimal quantity) {
        Inventory inventory = this.getById(id);
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }
        
        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("库存不足");
        }
        
        inventory.setQuantity(inventory.getQuantity().subtract(quantity));
        this.updateById(inventory);
        
        // 更新状态
        updateInventoryStatus(id);
    }
}








package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.entity.StorageLocation;
import com.lab.reagent.mapper.InventoryMapper;
import com.lab.reagent.mapper.StorageLocationMapper;
import com.lab.reagent.vo.InventoryVO;
import com.lab.reagent.vo.LocationStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 库存Service
 */
@Service
public class InventoryService extends ServiceImpl<InventoryMapper, Inventory> {
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    @Autowired
    private StorageLocationMapper locationMapper;
    
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
     * 获取位置统计信息
     */
    public List<LocationStatisticsVO> getLocationStatistics() {
        try {
            // 获取所有位置
            LambdaQueryWrapper<StorageLocation> locationWrapper = new LambdaQueryWrapper<>();
            locationWrapper.orderByAsc(StorageLocation::getRoomName)
                          .orderByAsc(StorageLocation::getCabinetNo)
                          .orderByAsc(StorageLocation::getShelfNo);
            List<StorageLocation> locations = locationMapper.selectList(locationWrapper);
            
            // 获取所有库存数据
            List<InventoryVO> allInventory = inventoryMapper.selectInventoryList(null);
            
            // 按位置分组统计（通过locationId匹配）
            List<LocationStatisticsVO> statistics = new java.util.ArrayList<>();
            
            for (StorageLocation location : locations) {
                LocationStatisticsVO stat = new LocationStatisticsVO();
                stat.setLocationId(location.getId());
                stat.setRoomName(location.getRoomName());
                stat.setCabinetNo(location.getCabinetNo());
                stat.setShelfNo(location.getShelfNo());
                stat.setFullLocation(location.getFullLocation());
                stat.setDescription(location.getDescription());
                
                // 统计该位置的试剂（需要通过locationId匹配，但InventoryVO中没有locationId）
                // 所以我们需要查询Inventory表
                LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
                invWrapper.eq(Inventory::getLocationId, location.getId());
                List<Inventory> locationInventories = this.list(invWrapper);
                
                Set<Long> reagentIds = new HashSet<>();
                BigDecimal totalQty = BigDecimal.ZERO;
                
                for (Inventory inv : locationInventories) {
                    if (inv.getReagentId() != null) {
                        reagentIds.add(inv.getReagentId());
                    }
                    if (inv.getQuantity() != null) {
                        totalQty = totalQty.add(inv.getQuantity());
                    }
                }
                
                stat.setReagentCount(reagentIds.size());
                stat.setTotalQuantity(totalQty);
                
                // 计算占用率（假设每个位置最大容量为20种试剂）
                int maxCapacity = 20;
                int rate = (int) ((stat.getReagentCount().doubleValue() / maxCapacity) * 100);
                stat.setOccupancyRate(Math.min(rate, 100));
                
                statistics.add(stat);
            }
            
            return statistics;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取位置统计信息失败：" + e.getMessage(), e);
        }
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








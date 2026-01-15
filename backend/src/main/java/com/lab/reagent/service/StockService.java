package com.lab.reagent.service;

import com.lab.reagent.dto.StockInDTO;
import com.lab.reagent.entity.*;
import com.lab.reagent.mapper.StockInRecordMapper;
import com.lab.reagent.mapper.StockOutRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 出入库Service
 */
@Service
public class StockService {
    
    @Autowired
    private StockInRecordMapper stockInRecordMapper;
    
    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ReagentService reagentService;
    
    /**
     * 入库
     */
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(StockInDTO record) {
        // 如果是新试剂（没有reagentId），先创建试剂
        if (record.getReagentId() == null) {
            if (record.getReagentName() == null || record.getReagentName().trim().isEmpty()) {
                throw new RuntimeException("新试剂入库必须填写试剂名称");
            }
            Reagent newReagent = new Reagent();
            newReagent.setName(record.getReagentName());
            newReagent.setCasNo(record.getCasNo());
            newReagent.setSpecification(record.getSpecification());
            newReagent.setUnit(record.getUnit());
            newReagent.setDangerLevel(record.getDangerLevel());
            newReagent.setCategoryId(record.getCategoryId());
            newReagent.setManufacturer(record.getManufacturer());
            newReagent.setSupplierLeadTime(record.getSupplierLeadTime());
            newReagent.setCreateTime(LocalDateTime.now());
            newReagent.setUpdateTime(LocalDateTime.now());
            
            reagentService.save(newReagent);
            record.setReagentId(newReagent.getId());
        }

        // 保存入库记录
        stockInRecordMapper.insert(record);
        
        // 更新库存
        if (record.getInventoryId() != null) {
            // 更新现有库存
            inventoryService.increaseStock(record.getInventoryId(), record.getQuantity());
        } else {
            // 创建新库存
            Inventory inventory = new Inventory();
            inventory.setReagentId(record.getReagentId());
            inventory.setLocationId(record.getLocationId());
            inventory.setBatchNo(record.getBatchNo());
            inventory.setQuantity(record.getQuantity());
            inventory.setWarningThreshold(java.math.BigDecimal.TEN);
            inventory.setExpiryDate(record.getExpiryDate());
            inventory.setSupplier(record.getSupplier());
            inventory.setPurchasePrice(record.getPurchasePrice());
            inventory.setPurchaseDate(java.time.LocalDate.now());
            inventory.setStatus("NORMAL");
            
            inventoryService.save(inventory);
            inventoryService.updateInventoryStatus(inventory.getId());
        }
    }
    
    /**
     * 出库
     */
    @Transactional(rollbackFor = Exception.class)
    public void stockOut(StockOutRecord record) {
        // 获取库存信息，设置试剂ID
        Inventory inventory = inventoryService.getById(record.getInventoryId());
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }
        record.setReagentId(inventory.getReagentId());
        
        // 保存出库记录
        stockOutRecordMapper.insert(record);
        
        // 更新库存
        inventoryService.decreaseStock(record.getInventoryId(), record.getQuantity());
        
        // 如果有申请单，更新申请状态
        if (record.getApplicationId() != null) {
            Application application = applicationService.getById(record.getApplicationId());
            if (application != null && "APPROVED".equals(application.getStatus())) {
                application.setStatus("COMPLETED");
                applicationService.updateById(application);
            }
        }
    }
}






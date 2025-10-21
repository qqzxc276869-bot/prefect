package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.service.InventoryService;
import com.lab.reagent.vo.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存Controller
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * 查询库存列表
     */
    @GetMapping("/list")
    public Result<List<InventoryVO>> list(@RequestParam(required = false) String name) {
        try {
            List<InventoryVO> list = inventoryService.getInventoryList(name);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询预警列表
     */
    @GetMapping("/warning")
    public Result<List<InventoryVO>> warning() {
        try {
            List<InventoryVO> list = inventoryService.getWarningList();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询库存详情
     */
    @GetMapping("/detail/{id}")
    public Result<Inventory> detail(@PathVariable Long id) {
        try {
            Inventory inventory = inventoryService.getById(id);
            return Result.success(inventory);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新库存预警阈值
     */
    @PutMapping("/threshold/{id}")
    public Result<String> updateThreshold(@PathVariable Long id, @RequestBody Inventory inventory) {
        try {
            Inventory entity = inventoryService.getById(id);
            if (entity == null) {
                return Result.error("库存不存在");
            }
            
            entity.setWarningThreshold(inventory.getWarningThreshold());
            inventoryService.updateById(entity);
            inventoryService.updateInventoryStatus(id);
            
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}







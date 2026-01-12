package com.lab.reagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 查询库存列表（支持分页和排序）
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) String name,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String sortField,
                          @RequestParam(required = false) String sortOrder,
                          @RequestParam(required = false) Integer page,
                          @RequestParam(required = false) Integer size) {
        try {
            // 如果提供了分页参数，返回分页结果
            if (page != null && size != null) {
                Page<InventoryVO> pageResult = inventoryService.getInventoryPage(page, size, name, status, sortField, sortOrder);
                return Result.success(pageResult);
            }
            // 否则返回全部数据（向后兼容）
            List<InventoryVO> list = inventoryService.getInventoryList(name, status, sortField, sortOrder);
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








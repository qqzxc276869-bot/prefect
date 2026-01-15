package com.lab.reagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.StockInRecord;
import com.lab.reagent.entity.StockOutRecord;
import com.lab.reagent.mapper.StockInRecordMapper;
import com.lab.reagent.mapper.StockOutRecordMapper;
import com.lab.reagent.service.StockService;
import com.lab.reagent.vo.StockInVO;
import com.lab.reagent.vo.StockOutVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 出入库Controller
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private StockInRecordMapper stockInRecordMapper;
    
    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;

    @Autowired
    private com.lab.reagent.service.ReagentService reagentService;

    @Autowired
    private com.lab.reagent.service.WasteRecordService wasteRecordService;

    @Autowired
    private com.lab.reagent.service.InventoryService inventoryService;

    @Autowired
    private com.lab.reagent.mapper.InventoryMapper inventoryMapper;
    
    /**
     * 入库
     */
    @PostMapping("/in")
    public Result<String> stockIn(@RequestBody com.lab.reagent.dto.StockInDTO record,
                                   @RequestHeader("userId") Long userId,
                                   @RequestHeader("realName") String realName) {
        try {
            // URL解码中文名称
            String decodedName = URLDecoder.decode(realName, StandardCharsets.UTF_8.name());
            record.setOperatorId(userId);
            record.setOperatorName(decodedName);
            stockService.stockIn(record);
            return Result.success("入库成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 出库
     */
    @PostMapping("/out")
    public Result<String> stockOut(@RequestBody StockOutRecord record,
                                    @RequestHeader("userId") Long userId,
                                    @RequestHeader("realName") String realName) {
        try {
            // URL解码中文名称
            String decodedName = URLDecoder.decode(realName, StandardCharsets.UTF_8.name());
            record.setOperatorId(userId);
            record.setOperatorName(decodedName);
            stockService.stockOut(record);
            return Result.success("出库成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 废弃处理
     */
    @PostMapping("/discard")
    public Result<String> discard(@RequestBody com.lab.reagent.dto.DiscardDTO dto,
                                  @RequestHeader("userId") Long userId,
                                  @RequestHeader("realName") String realName) {
        try {
            com.lab.reagent.entity.Inventory inventory = inventoryMapper.selectById(dto.getInventoryId());
            if (inventory == null) {
                return Result.error("库存不存在");
            }
            if (inventory.getQuantity().compareTo(dto.getQuantity()) < 0) {
                return Result.error("废弃数量不能大于当前库存");
            }
            
            String decodedName = URLDecoder.decode(realName, StandardCharsets.UTF_8.name());

            // 1. 创建废弃记录
            com.lab.reagent.entity.WasteRecord wasteRecord = new com.lab.reagent.entity.WasteRecord();
            wasteRecord.setReagentId(inventory.getReagentId());
            com.lab.reagent.entity.Reagent r = reagentService.getById(inventory.getReagentId());
            wasteRecord.setReagentName(r != null ? r.getName() : "");
            wasteRecord.setInventoryId(inventory.getId());
            wasteRecord.setQuantity(dto.getQuantity());
            wasteRecord.setMethod(dto.getMethod());
            wasteRecord.setOperatorId(userId);
            wasteRecord.setOperatorName(decodedName);
            wasteRecord.setRemark(dto.getRemark());
            wasteRecord.setCreateTime(java.time.LocalDateTime.now());
            wasteRecordService.save(wasteRecord);

            // 2. 扣减库存
            inventory.setQuantity(inventory.getQuantity().subtract(dto.getQuantity()));
            if (inventory.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                inventory.setQuantity(java.math.BigDecimal.ZERO); // Ensure it's exactly 0
                inventory.setStatus("DISCARDED");
            }
            inventoryService.updateById(inventory);

            return Result.success("废弃处理成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 入库记录查询
     */
    @GetMapping("/in/list")
    public Result<Page<StockInVO>> inList(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<StockInVO> pageInfo = new Page<>(page, size);
            Page<StockInVO> result = stockInRecordMapper.selectPageWithVO(pageInfo);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 出库记录查询
     */
    @GetMapping("/out/list")
    public Result<Page<StockOutVO>> outList(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<StockOutVO> pageInfo = new Page<>(page, size);
            Page<StockOutVO> result = stockOutRecordMapper.selectPageWithVO(pageInfo);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}







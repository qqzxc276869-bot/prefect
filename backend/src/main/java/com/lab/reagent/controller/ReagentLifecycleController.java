package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.entity.ReagentLifecycle;
import com.lab.reagent.service.ReagentLifecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 试剂生命周期 Controller
 */
@RestController
@RequestMapping("/lifecycle")
@CrossOrigin
public class ReagentLifecycleController {
    
    @Autowired
    private ReagentLifecycleService lifecycleService;
    
    /**
     * 创建生命周期记录
     */
    @PostMapping("/create")
    public Result create(@RequestBody ReagentLifecycle lifecycle) {
        int result = lifecycleService.create(lifecycle);
        return result > 0 ? Result.success("创建成功", lifecycle) : Result.error("创建失败");
    }
    
    /**
     * 扫码查询
     */
    @GetMapping("/scan/{qrCode}")
    public Result scanQrCode(@PathVariable String qrCode, 
                            @RequestParam(required = false) Long userId,
                            @RequestParam(required = false) String userName) {
        ReagentLifecycle lifecycle = lifecycleService.getByQrCode(qrCode);
        
        // 记录扫码日志
        if (userId != null) {
            lifecycleService.logScan(qrCode, "REAGENT", "VIEW", userId, userName, 
                lifecycle != null ? "SUCCESS" : "FAILED");
        }
        
        return lifecycle != null ? Result.success(lifecycle) : Result.error("未找到该试剂");
    }
    
    /**
     * 标记为已开封
     */
    @PostMapping("/mark-opened")
    public Result markOpened(@RequestBody Map<String, Object> params) {
        String qrCode = params.get("qrCode").toString();
        Long userId = Long.valueOf(params.get("userId").toString());
        String userName = params.get("userName").toString();
        Integer validityDays = params.get("validityDays") != null ? 
            Integer.valueOf(params.get("validityDays").toString()) : null;
        
        boolean result = lifecycleService.markAsOpened(qrCode, userId, userName, validityDays);
        
        // 记录扫码日志
        lifecycleService.logScan(qrCode, "REAGENT", "OPEN", userId, userName, 
            result ? "SUCCESS" : "FAILED");
        
        return result ? Result.success("标记成功") : Result.error("操作失败");
    }
    
    /**
     * 记录使用
     */
    @PostMapping("/record-usage")
    public Result recordUsage(@RequestBody Map<String, Object> params) {
        String qrCode = params.get("qrCode").toString();
        BigDecimal usedQuantity = params.get("usedQuantity") != null ? 
            new BigDecimal(params.get("usedQuantity").toString()) : null;
        
        boolean result = lifecycleService.recordUsage(qrCode, usedQuantity);
        return result ? Result.success("记录成功") : Result.error("操作失败");
    }
    
    /**
     * 转移库位
     */
    @PostMapping("/transfer")
    public Result transfer(@RequestBody Map<String, Object> params) {
        String qrCode = params.get("qrCode").toString();
        Long newLocationId = Long.valueOf(params.get("newLocationId").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "转移";
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
        String userName = params.get("userName") != null ? params.get("userName").toString() : null;
        
        boolean result = lifecycleService.transferLocation(qrCode, newLocationId, reason);
        
        // 记录扫码日志
        if (userId != null) {
            lifecycleService.logScan(qrCode, "REAGENT", "TRANSFER", userId, userName, 
                result ? "SUCCESS" : "FAILED");
        }
        
        return result ? Result.success("转移成功") : Result.error("操作失败");
    }
}


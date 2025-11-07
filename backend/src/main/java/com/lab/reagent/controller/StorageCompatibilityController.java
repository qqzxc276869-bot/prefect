package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.StorageCompatibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 存储兼容性检查 Controller
 */
@RestController
@RequestMapping("/storage/compatibility")
@CrossOrigin
public class StorageCompatibilityController {
    
    @Autowired
    private StorageCompatibilityService compatibilityService;
    
    /**
     * 检查存储兼容性
     */
    @PostMapping("/check")
    public Result checkCompatibility(@RequestBody Map<String, Object> params) {
        Long reagentId = Long.valueOf(params.get("reagentId").toString());
        Long locationId = Long.valueOf(params.get("locationId").toString());
        Long operatorId = params.get("operatorId") != null ? 
            Long.valueOf(params.get("operatorId").toString()) : null;
        String operatorName = params.get("operatorName") != null ? 
            params.get("operatorName").toString() : null;
        
        Map<String, Object> result = compatibilityService.checkCompatibility(
            reagentId, locationId, operatorId, operatorName
        );
        
        if ((Boolean) result.get("success")) {
            return Result.success(result);
        } else {
            return Result.error(result.get("message").toString(), result);
        }
    }
}


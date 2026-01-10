package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.FifoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FIFO先进先出Controller
 */
@RestController
@RequestMapping("/api/fifo")
public class FifoController {
    
    @Autowired
    private FifoService fifoService;
    
    /**
     * 获取FIFO建议
     */
    @GetMapping("/suggestion")
    public Result<Map<String, Object>> getSuggestion(@RequestParam Long reagentId) {
        try {
            Map<String, Object> suggestion = fifoService.getFifoSuggestion(reagentId);
            return Result.success(suggestion);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}





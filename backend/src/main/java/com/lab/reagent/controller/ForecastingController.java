package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.entity.ForecastingRecord;
import com.lab.reagent.service.ForecastingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能预测 Controller
 */
@RestController
@RequestMapping("/forecasting")
@CrossOrigin
public class ForecastingController {
    
    @Autowired
    private ForecastingService forecastingService;
    
    /**
     * 为指定试剂生成预测
     */
    @PostMapping("/generate/{reagentId}")
    public Result generateForecast(@PathVariable Long reagentId) {
        ForecastingRecord forecast = forecastingService.generateForecast(reagentId);
        return forecast != null ? Result.success(forecast) : Result.error("无法生成预测，可能是数据不足");
    }
    
    /**
     * 获取所有活跃预测
     */
    @GetMapping("/active")
    public Result getActiveForecasts() {
        List<ForecastingRecord> forecasts = forecastingService.getActiveForecasts();
        return Result.success(forecasts);
    }
    
    /**
     * 获取即将需要采购的试剂
     */
    @GetMapping("/upcoming")
    public Result getUpcoming() {
        List<ForecastingRecord> forecasts = forecastingService.getUpcomingOrders();
        return Result.success(forecasts);
    }
}


package com.lab.reagent.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.*;
import com.lab.reagent.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 智能预测服务
 */
@Service
public class ForecastingService {
    
    @Autowired
    private ForecastingRecordMapper forecastingRecordMapper;
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    @Autowired
    private ReagentMapper reagentMapper;
    
    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;
    
    @Autowired
    private ConsumptionPatternMapper consumptionPatternMapper;
    
    /**
     * 为指定试剂生成预测
     */
    public ForecastingRecord generateForecast(Long reagentId) {
        // 1. 获取试剂信息
        Reagent reagent = reagentMapper.selectById(reagentId);
        if (reagent == null) {
            return null;
        }
        
        // 2. 获取当前库存
        LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
        invWrapper.eq(Inventory::getReagentId, reagentId);
        List<Inventory> inventories = inventoryMapper.selectList(invWrapper);
        
        BigDecimal currentStock = inventories.stream()
            .map(Inventory::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
            return null;  // 库存为0，无需预测
        }
        
        // 3. 计算平均日消耗量（基于最近90天的数据）
        LocalDateTime startDate = LocalDateTime.now().minusDays(90);
        LambdaQueryWrapper<StockOutRecord> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(StockOutRecord::getReagentId, reagentId)
                  .ge(StockOutRecord::getCreateTime, startDate);
        List<StockOutRecord> stockOutRecords = stockOutRecordMapper.selectList(outWrapper);
        
        if (stockOutRecords.isEmpty()) {
            return null;  // 无消耗记录
        }
        
        BigDecimal totalConsumption = stockOutRecords.stream()
            .map(StockOutRecord::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long days = ChronoUnit.DAYS.between(startDate.toLocalDate(), LocalDate.now());
        if (days == 0) days = 1;
        
        BigDecimal averageDailyConsumption = totalConsumption.divide(
            BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP
        );
        
        if (averageDailyConsumption.compareTo(BigDecimal.ZERO) <= 0) {
            return null;  // 无有效消耗
        }
        
        // 4. 预测耗尽日期
        BigDecimal daysUntilDepletion = currentStock.divide(averageDailyConsumption, 0, RoundingMode.UP);
        LocalDate depletionDate = LocalDate.now().plusDays(daysUntilDepletion.intValue());
        
        // 5. 获取供应商到货周期
        Integer supplierLeadTime = reagent.getSupplierLeadTime();
        if (supplierLeadTime == null) {
            supplierLeadTime = 7;  // 默认7天
        }
        
        // 6. 计算建议下单日期（提前供应商到货周期 + 缓冲时间3天）
        LocalDate recommendedOrderDate = depletionDate.minusDays(supplierLeadTime + 3);
        
        // 7. 建议订购数量（根据平均消耗量，建议采购30天用量）
        BigDecimal recommendedQuantity = averageDailyConsumption.multiply(BigDecimal.valueOf(30))
            .setScale(0, RoundingMode.UP);
        
        // 8. 计算置信度（基于数据量和消耗稳定性）
        BigDecimal confidenceLevel = calculateConfidence(stockOutRecords);
        
        // 9. 创建预测记录
        ForecastingRecord forecast = new ForecastingRecord();
        forecast.setReagentId(reagentId);
        forecast.setReagentName(reagent.getName());
        forecast.setCurrentStock(currentStock);
        forecast.setPredictedDepletionDate(depletionDate);
        forecast.setDaysUntilDepletion(daysUntilDepletion.intValue());
        forecast.setAverageDailyConsumption(averageDailyConsumption);
        forecast.setSupplierLeadTime(supplierLeadTime);
        forecast.setRecommendedOrderDate(recommendedOrderDate);
        forecast.setRecommendedOrderQuantity(recommendedQuantity);
        forecast.setConfidenceLevel(confidenceLevel);
        forecast.setPredictionModel("LINEAR_TREND");
        
        // 预测依据
        Map<String, Object> basis = new HashMap<>();
        basis.put("analysisStartDate", startDate.toLocalDate().toString());
        basis.put("analysisEndDate", LocalDate.now().toString());
        basis.put("totalConsumption", totalConsumption);
        basis.put("consumptionRecords", stockOutRecords.size());
        basis.put("analysisDays", days);
        
        forecast.setPredictionBasis(JSON.toJSONString(basis));
        forecast.setStatus("ACTIVE");
        
        // 保存预测记录
        forecastingRecordMapper.insert(forecast);
        
        return forecast;
    }
    
    /**
     * 计算预测置信度
     */
    private BigDecimal calculateConfidence(List<StockOutRecord> records) {
        if (records.size() < 3) {
            return BigDecimal.valueOf(50);  // 数据太少，置信度50%
        }
        
        // 计算消耗量的标准差，标准差越小，置信度越高
        double mean = records.stream()
            .mapToDouble(r -> r.getQuantity().doubleValue())
            .average().orElse(0);
        
        double variance = records.stream()
            .mapToDouble(r -> Math.pow(r.getQuantity().doubleValue() - mean, 2))
            .average().orElse(0);
        
        double stdDev = Math.sqrt(variance);
        double cv = mean == 0 ? 0 : stdDev / mean;  // 变异系数
        
        // 变异系数越小，置信度越高
        double confidence = Math.max(50, Math.min(95, 90 - cv * 50));
        
        return BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 获取所有活跃的预测
     */
    public List<ForecastingRecord> getActiveForecasts() {
        LambdaQueryWrapper<ForecastingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForecastingRecord::getStatus, "ACTIVE")
               .orderByAsc(ForecastingRecord::getRecommendedOrderDate);
        return forecastingRecordMapper.selectList(wrapper);
    }
    
    /**
     * 获取即将需要采购的试剂（建议下单日期在未来7天内）
     */
    public List<ForecastingRecord> getUpcomingOrders() {
        LambdaQueryWrapper<ForecastingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForecastingRecord::getStatus, "ACTIVE")
               .le(ForecastingRecord::getRecommendedOrderDate, LocalDate.now().plusDays(7))
               .ge(ForecastingRecord::getRecommendedOrderDate, LocalDate.now())
               .orderByAsc(ForecastingRecord::getRecommendedOrderDate);
        return forecastingRecordMapper.selectList(wrapper);
    }
}


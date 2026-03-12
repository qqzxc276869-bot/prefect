package com.lab.reagent.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.*;
import com.lab.reagent.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能预测服务
 */
@Slf4j
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
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private TimeService timeService;
    
    /**
     * 为指定试剂生成预测（使用AI模型）
     */
    public ForecastingRecord generateForecast(Long reagentId) {
        return generateForecastWithAI(reagentId);
    }
    
    /**
     * 使用AI模型生成预测
     */
    public ForecastingRecord generateForecastWithAI(Long reagentId) {
        try {
            // 1. 获取试剂信息
            Reagent reagent = reagentMapper.selectById(reagentId);
            if (reagent == null) {
                throw new RuntimeException("试剂不存在");
            }
            
            // 2. 获取当前库存
            LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
            invWrapper.eq(Inventory::getReagentId, reagentId);
            List<Inventory> inventories = inventoryMapper.selectList(invWrapper);
            
            BigDecimal currentStock = inventories.stream()
                .map(Inventory::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("当前库存为0，无需预测");
            }
            
            // 3. 获取历史消耗数据（先尝试最近180天，如果数据不足则扩大范围）
            LocalDateTime startDate = LocalDateTime.now().minusDays(180);
            LambdaQueryWrapper<StockOutRecord> outWrapper = new LambdaQueryWrapper<>();
            outWrapper.eq(StockOutRecord::getReagentId, reagentId)
                      .ge(StockOutRecord::getCreateTime, startDate)
                      .orderByAsc(StockOutRecord::getCreateTime);
            List<StockOutRecord> stockOutRecords = stockOutRecordMapper.selectList(outWrapper);
            
            // 如果数据不足，尝试扩大查询范围到365天
            if (stockOutRecords.isEmpty() || stockOutRecords.size() < 3) {
                startDate = LocalDateTime.now().minusDays(365);
                outWrapper = new LambdaQueryWrapper<>();
                outWrapper.eq(StockOutRecord::getReagentId, reagentId)
                          .ge(StockOutRecord::getCreateTime, startDate)
                          .orderByAsc(StockOutRecord::getCreateTime);
                stockOutRecords = stockOutRecordMapper.selectList(outWrapper);
            }
            
            // 如果仍然没有数据，尝试获取所有历史记录
            if (stockOutRecords.isEmpty()) {
                outWrapper = new LambdaQueryWrapper<>();
                outWrapper.eq(StockOutRecord::getReagentId, reagentId)
                          .orderByAsc(StockOutRecord::getCreateTime);
                stockOutRecords = stockOutRecordMapper.selectList(outWrapper);
            }
            
            if (stockOutRecords.isEmpty()) {
                // 即使没有历史数据，也尝试使用AI进行基础预测
                log.warn("试剂ID: {} 无历史消耗记录，将使用基础信息进行AI预测", reagentId);
                return generateForecastWithLimitedData(reagent, currentStock);
            }
            
            // 4. 准备历史数据供AI分析
            List<Map<String, Object>> consumptionHistory = stockOutRecords.stream()
                .map(record -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", record.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    item.put("quantity", record.getQuantity());
                    item.put("purpose", record.getPurpose());
                    return item;
                })
                .collect(Collectors.toList());
            
            // 5. 计算基础统计数据
            BigDecimal totalConsumption = stockOutRecords.stream()
                .map(StockOutRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long days = ChronoUnit.DAYS.between(startDate.toLocalDate(), LocalDate.now());
            if (days == 0) days = 1;
            
            BigDecimal averageDailyConsumption = totalConsumption.divide(
                BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP
            );
            
            // 6. 构建AI提示词
            Map<String, Object> dataForAI = new HashMap<>();
            dataForAI.put("currentDate", LocalDate.now().toString());  // 明确告知AI当前日期
            dataForAI.put("reagentName", reagent.getName());
            dataForAI.put("reagentSpecification", reagent.getSpecification());
            dataForAI.put("currentStock", currentStock);
            dataForAI.put("unit", reagent.getUnit() != null ? reagent.getUnit() : "瓶");
            dataForAI.put("supplierLeadTime", reagent.getSupplierLeadTime() != null ? reagent.getSupplierLeadTime() : 7);
            dataForAI.put("consumptionHistory", consumptionHistory);
            dataForAI.put("totalConsumption", totalConsumption);
            dataForAI.put("averageDailyConsumption", averageDailyConsumption);
            dataForAI.put("analysisPeriodDays", days);
            dataForAI.put("analysisStartDate", startDate.toLocalDate().toString());
            dataForAI.put("analysisEndDate", LocalDate.now().toString());
            
            // 获取系统时间（作为项目统一的时间参照）
            LocalDateTime systemTime = timeService.getBeijingTime();
            String timeDescription = timeService.getFormattedTimeDescription();
            
            String systemPrompt = "你是实验室试剂消耗预测专家。根据历史消耗数据,预测未来30天的消耗趋势。\n" +
                    "**关键时间信息**：\n" +
                    "- " + timeDescription + "\n" +
                    "- 当前年份：" + systemTime.getYear() + "年\n" +
                    "- 当前月份：" + systemTime.getMonthValue() + "月\n" +
                    "- 重要提示：我们现在已经是" + systemTime.getYear() + "年了，所有" + (systemTime.getYear() - 1) + "年的日期都已经是过去了。\n" +
                    "- 你预测的所有日期都必须是" + systemTime.toLocalDate() + "之后的未来日期。\n" +
                    "\n" +
                    "请严格返回JSON格式，包含以下字段：\n" +
                    "{\n" +
                    "  \"predictedDailyConsumption\": 预测的日均消耗量（数字）,\n" +
                    "  \"predictedTotalConsumption\": 未来30天预测总消耗量（数字）,\n" +
                    "  \"daysUntilDepletion\": 从今天（" + systemTime.toLocalDate() + "）开始计算，预计耗尽天数（整数）,\n" +
                    "  \"predictedDepletionDate\": 预计耗尽日期（YYYY-MM-DD格式，必须是" + systemTime.getYear() + "年或之后的日期）,\n" +
                    "  \"recommendedOrderQuantity\": 建议订购数量（数字）,\n" +
                    "  \"recommendedOrderDate\": 建议下单日期（YYYY-MM-DD格式，必须是" + systemTime.getYear() + "年或之后，且晚于" + systemTime.toLocalDate() + "）,\n" +
                    "  \"confidenceLevel\": 预测置信度（0-100的整数）,\n" +
                    "  \"trendAnalysis\": 消耗趋势分析（字符串）,\n" +
                    "  \"riskFactors\": 风险因素说明（字符串数组）,\n" +
                    "  \"suggestions\": 采购建议（字符串数组）\n" +
                    "}";
            
            String userPrompt = "请分析以下试剂的历史消耗数据，并生成预测：\n" + JSON.toJSONString(dataForAI, true);
            
            // 7. 调用AI服务
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);
            
            String aiResponse = aiService.chatRaw(null, messages);
            
            // 8. 解析AI返回的JSON
            String jsonContent = extractJsonFromResponse(aiResponse);
            Map<String, Object> aiResult;
            try {
                aiResult = JSON.parseObject(jsonContent, Map.class);
            } catch (Exception e) {
                log.warn("解析AI返回的JSON失败，内容: {}", jsonContent);
                aiResult = new HashMap<>();
            }
            
            // 9. 创建预测记录
            ForecastingRecord forecast = new ForecastingRecord();
            forecast.setReagentId(reagentId);
            forecast.setReagentName(reagent.getName());
            forecast.setCurrentStock(currentStock);
            
            // 安全地从AI结果中提取数据
            LocalDate depDate = safeParseDate(aiResult.get("predictedDepletionDate"));
            LocalDate today = LocalDate.now();
            
            // 验证日期不能是过去的日期
            if (depDate == null || depDate.isBefore(today)) {
                log.warn("AI返回的耗尽日期无效或已过期: {}，将基于当前日期重新计算", depDate);
                // 默认降级：基于平均日消耗计算
                BigDecimal daily = averageDailyConsumption.compareTo(BigDecimal.ZERO) > 0 ? averageDailyConsumption : new BigDecimal("0.5");
                int daysUntil = currentStock.divide(daily, 0, RoundingMode.UP).intValue();
                depDate = today.plusDays(daysUntil);
            }
            forecast.setPredictedDepletionDate(depDate);
            
            Integer daysUntil = safeParseInt(aiResult.get("daysUntilDepletion"));
            if (daysUntil == null || daysUntil < 0) {
                daysUntil = (int) ChronoUnit.DAYS.between(today, depDate);
            }
            forecast.setDaysUntilDepletion(daysUntil);
            
            BigDecimal predDaily = safeParseBigDecimal(aiResult.get("predictedDailyConsumption"));
            forecast.setAverageDailyConsumption(predDaily != null ? predDaily : averageDailyConsumption);
            
            Integer supplierLeadTime = reagent.getSupplierLeadTime() != null ? reagent.getSupplierLeadTime() : 7;
            forecast.setSupplierLeadTime(supplierLeadTime);
            
            LocalDate orderDate = safeParseDate(aiResult.get("recommendedOrderDate"));
            // 验证建议下单日期
            if (orderDate == null || orderDate.isBefore(today)) {
                log.warn("AI返回的建议下单日期无效或已过期: {}，将基于耗尽日期重新计算", orderDate);
                orderDate = depDate.minusDays(supplierLeadTime + 2);
                // 如果计算出的下单日期也是过去的，则设置为今天
                if (orderDate.isBefore(today)) {
                    orderDate = today;
                }
            }
            forecast.setRecommendedOrderDate(orderDate);
            
            BigDecimal orderQty = safeParseBigDecimal(aiResult.get("recommendedOrderQuantity"));
            if (orderQty == null) {
                orderQty = forecast.getAverageDailyConsumption().multiply(new BigDecimal("30")).setScale(0, RoundingMode.UP);
            }
            forecast.setRecommendedOrderQuantity(orderQty);

            BigDecimal confidence = safeParseBigDecimal(aiResult.get("confidenceLevel"));
            forecast.setConfidenceLevel(confidence != null ? confidence : calculateConfidence(stockOutRecords));
            
            forecast.setPredictionModel("AI_MODEL");
            
            // 保存AI分析结果
            Map<String, Object> basis = new HashMap<>();
            basis.put("aiAnalysis", aiResult);
            basis.put("analysisStartDate", startDate.toLocalDate().toString());
            basis.put("analysisEndDate", LocalDate.now().toString());
            basis.put("totalConsumption", totalConsumption);
            basis.put("consumptionRecords", stockOutRecords.size());
            basis.put("analysisDays", days);
            basis.put("aiResponse", aiResponse);
            
            forecast.setPredictionBasis(JSON.toJSONString(basis));
            forecast.setStatus("ACTIVE");
            
            // 保存预测记录
            forecastingRecordMapper.insert(forecast);
            
            log.info("AI预测完成，试剂ID: {}, 预测结果: {}", reagentId, forecast);
            return forecast;
            
        } catch (Exception e) {
            log.error("AI预测失败，试剂ID: {}", reagentId, e);
            // 如果AI预测失败，回退到传统方法
            try {
                ForecastingRecord traditionalForecast = generateForecastTraditional(reagentId);
                if (traditionalForecast != null) {
                    return traditionalForecast;
                }
            } catch (Exception ex) {
                log.error("传统预测方法也失败，试剂ID: {}", reagentId, ex);
            }
            // 如果传统方法也失败，尝试使用基础预测
            Reagent reagent = reagentMapper.selectById(reagentId);
            if (reagent != null) {
                LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
                invWrapper.eq(Inventory::getReagentId, reagentId);
                List<Inventory> inventories = inventoryMapper.selectList(invWrapper);
                BigDecimal currentStock = inventories.stream()
                    .map(Inventory::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (currentStock.compareTo(BigDecimal.ZERO) > 0) {
                    return generateForecastWithLimitedData(reagent, currentStock);
                }
            }
            throw new RuntimeException("无法生成预测：数据不足或AI服务异常。请确保：1)试剂有库存；2)有历史消耗记录；3)AI服务正常。");
        }
    }
    
    /**
     * 在数据有限的情况下生成基础预测
     */
    private ForecastingRecord generateForecastWithLimitedData(Reagent reagent, BigDecimal currentStock) {
        try {
            log.info("使用基础预测方法，试剂: {}, 当前库存: {}", reagent.getName(), currentStock);
            
            // 使用AI进行基础预测，即使没有历史数据
            Map<String, Object> dataForAI = new HashMap<>();
            dataForAI.put("currentDate", LocalDate.now().toString());  // 明确当前日期
            dataForAI.put("reagentName", reagent.getName());
            dataForAI.put("reagentSpecification", reagent.getSpecification());
            dataForAI.put("currentStock", currentStock);
            dataForAI.put("unit", reagent.getUnit() != null ? reagent.getUnit() : "瓶");
            dataForAI.put("supplierLeadTime", reagent.getSupplierLeadTime() != null ? reagent.getSupplierLeadTime() : 7);
            dataForAI.put("hasHistoryData", false);
            dataForAI.put("note", "该试剂暂无历史消耗记录，将基于试剂特性和库存情况进行基础预测");
            
            // 获取系统时间（作为项目统一的时间参照）
            LocalDateTime systemTime = timeService.getBeijingTime();
            String timeDescription = timeService.getFormattedTimeDescription();
            
            String systemPrompt = "你是实验室试剂消耗预测专家。由于该试剂暂无历史消耗数据，请基于试剂特性和当前库存情况，提供合理的预测建议。\n" +
                    "**关键时间信息**：\n" +
                    "- " + timeDescription + "\n" +
                    "- 当前年份：" + systemTime.getYear() + "年\n" +
                    "- 当前月份：" + systemTime.getMonthValue() + "月\n" +
                    "- 重要提示：我们现在已经是" + systemTime.getYear() + "年了，所有" + (systemTime.getYear() - 1) + "年的日期都已经是过去了。\n" +
                    "- 你预测的所有日期都必须是" + systemTime.toLocalDate() + "之后的未来日期。\n" +
                    "\n" +
                    "请严格返回JSON格式，包含以下字段：\n" +
                    "{\n" +
                    "  \"predictedDailyConsumption\": 预测的日均消耗量（数字，建议值0.1-1.0）,\n" +
                    "  \"predictedTotalConsumption\": 未来30天预测总消耗量（数字）,\n" +
                    "  \"daysUntilDepletion\": 从今天（" + systemTime.toLocalDate() + "）开始计算的预计耗尽天数（整数）,\n" +
                    "  \"predictedDepletionDate\": 预计耗尽日期（YYYY-MM-DD格式，必须是" + systemTime.getYear() + "年或之后的日期）,\n" +
                    "  \"recommendedOrderQuantity\": 建议订购数量（数字，建议30-60天用量）,\n" +
                    "  \"recommendedOrderDate\": 建议下单日期（YYYY-MM-DD格式，必须是" + systemTime.getYear() + "年或之后，且晚于" + systemTime.toLocalDate() + "）,\n" +
                    "  \"confidenceLevel\": 预测置信度（0-100的整数，数据不足时建议30-50）,\n" +
                    "  \"trendAnalysis\": 消耗趋势分析（字符串，说明数据不足的情况）,\n" +
                    "  \"riskFactors\": 风险因素说明（字符串数组）,\n" +
                    "  \"suggestions\": 采购建议（字符串数组）\n" +
                    "}";
            String userPrompt = "请基于以下试剂信息进行基础预测：\n" + JSON.toJSONString(dataForAI, true);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);
            
            String aiResponse = aiService.chatRaw(null, messages);
            String jsonContent = extractJsonFromResponse(aiResponse);
            Map<String, Object> aiResult;
            try {
                aiResult = JSON.parseObject(jsonContent, Map.class);
            } catch (Exception e) {
                log.warn("解析AI基础预测JSON失败: {}", jsonContent);
                aiResult = new HashMap<>();
            }
            
            // 创建预测记录
            ForecastingRecord forecast = new ForecastingRecord();
            forecast.setReagentId(reagent.getId());
            forecast.setReagentName(reagent.getName());
            forecast.setCurrentStock(currentStock);
            
            // 安全提取
            LocalDate depDate = safeParseDate(aiResult.get("predictedDepletionDate"));
            LocalDate today = LocalDate.now();
            
            // 验证日期不能是过去的日期
            if (depDate == null || depDate.isBefore(today)) {
                log.warn("AI返回的耗尽日期无效或已过期: {}，将基于当前日期重新计算", depDate);
                BigDecimal daily = new BigDecimal("0.5");
                int days = currentStock.divide(daily, 0, RoundingMode.UP).intValue();
                depDate = today.plusDays(days);
            }
            forecast.setPredictedDepletionDate(depDate);
            
            Integer daysUntil = safeParseInt(aiResult.get("daysUntilDepletion"));
            if (daysUntil == null || daysUntil < 0) {
                daysUntil = (int) ChronoUnit.DAYS.between(today, depDate);
            }
            forecast.setDaysUntilDepletion(daysUntil);
            
            BigDecimal predDaily = safeParseBigDecimal(aiResult.get("predictedDailyConsumption"));
            forecast.setAverageDailyConsumption(predDaily != null ? predDaily : new BigDecimal("0.5"));
            
            Integer leadTime = reagent.getSupplierLeadTime() != null ? reagent.getSupplierLeadTime() : 7;
            forecast.setSupplierLeadTime(leadTime);
            
            LocalDate orderDate = safeParseDate(aiResult.get("recommendedOrderDate"));
            // 验证建议下单日期
            if (orderDate == null || orderDate.isBefore(today)) {
                log.warn("AI返回的建议下单日期无效或已过期: {}，将基于耗尽日期重新计算", orderDate);
                orderDate = depDate.minusDays(leadTime + 3);
                // 如果计算出的下单日期也是过去的，则设置为今天
                if (orderDate.isBefore(today)) {
                    orderDate = today;
                }
            }
            forecast.setRecommendedOrderDate(orderDate);
            
            BigDecimal orderQty = safeParseBigDecimal(aiResult.get("recommendedOrderQuantity"));
            forecast.setRecommendedOrderQuantity(orderQty != null ? orderQty : forecast.getAverageDailyConsumption().multiply(new BigDecimal("30")));
            
            BigDecimal conf = safeParseBigDecimal(aiResult.get("confidenceLevel"));
            forecast.setConfidenceLevel(conf != null ? conf : new BigDecimal("40"));
            
            forecast.setPredictionModel("AI_MODEL_LIMITED_DATA");
            
            // 保存AI分析结果
            Map<String, Object> basis = new HashMap<>();
            basis.put("aiAnalysis", aiResult);
            basis.put("hasHistoryData", false);
            basis.put("note", "该预测基于基础信息生成，建议补充历史消耗数据以提高准确性");
            basis.put("aiResponse", aiResponse);
            
            forecast.setPredictionBasis(JSON.toJSONString(basis));
            forecast.setStatus("ACTIVE");
            
            // 保存预测记录
            forecastingRecordMapper.insert(forecast);
            
            log.info("基础预测完成，试剂ID: {}, 预测结果: {}", reagent.getId(), forecast);
            return forecast;
            
        } catch (Exception e) {
            log.error("基础预测也失败，试剂: {}", reagent.getName(), e);
            throw new RuntimeException("无法生成预测：AI服务异常或数据格式错误。错误信息：" + e.getMessage());
        }
    }
    
    /**
     * 传统预测方法（作为AI预测的备用方案）
     */
    private ForecastingRecord generateForecastTraditional(Long reagentId) {
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
            return null;
        }
        
        // 3. 计算平均日消耗量（基于最近90天的数据，如果不足则扩大范围）
        LocalDateTime startDate = LocalDateTime.now().minusDays(90);
        LambdaQueryWrapper<StockOutRecord> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(StockOutRecord::getReagentId, reagentId)
                  .ge(StockOutRecord::getCreateTime, startDate);
        List<StockOutRecord> stockOutRecords = stockOutRecordMapper.selectList(outWrapper);
        
        // 如果数据不足，扩大查询范围
        if (stockOutRecords.isEmpty()) {
            startDate = LocalDateTime.now().minusDays(365);
            outWrapper = new LambdaQueryWrapper<>();
            outWrapper.eq(StockOutRecord::getReagentId, reagentId)
                      .ge(StockOutRecord::getCreateTime, startDate);
            stockOutRecords = stockOutRecordMapper.selectList(outWrapper);
        }
        
        // 如果仍然没有数据，获取所有历史记录
        if (stockOutRecords.isEmpty()) {
            outWrapper = new LambdaQueryWrapper<>();
            outWrapper.eq(StockOutRecord::getReagentId, reagentId);
            stockOutRecords = stockOutRecordMapper.selectList(outWrapper);
        }
        
        if (stockOutRecords.isEmpty()) {
            return null; // 传统方法需要历史数据
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
            return null;
        }
        
        // 4. 预测耗尽日期
        BigDecimal daysUntilDepletion = currentStock.divide(averageDailyConsumption, 0, RoundingMode.UP);
        LocalDate depletionDate = LocalDate.now().plusDays(daysUntilDepletion.intValue());
        
        // 5. 获取供应商到货周期
        Integer supplierLeadTime = reagent.getSupplierLeadTime();
        if (supplierLeadTime == null) {
            supplierLeadTime = 7;
        }
        
        // 6. 计算建议下单日期
        LocalDate recommendedOrderDate = depletionDate.minusDays(supplierLeadTime + 3);
        
        // 7. 建议订购数量
        BigDecimal recommendedQuantity = averageDailyConsumption.multiply(BigDecimal.valueOf(30))
            .setScale(0, RoundingMode.UP);
        
        // 8. 计算置信度
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
     * 从AI响应中提取JSON内容
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("AI响应为空");
        }
        
        // 尝试提取JSON（可能包含在markdown代码块中）
        String trimmed = response.trim();
        
        // 如果包含```json或```，提取其中的内容
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        } else if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }
        
        // 尝试直接查找JSON对象
        int jsonStart = trimmed.indexOf("{");
        int jsonEnd = trimmed.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String potentialJson = trimmed.substring(jsonStart, jsonEnd + 1);
            // 简单校验是否基本符合JSON结构
            if (potentialJson.contains("\"") && potentialJson.contains(":")) {
                return potentialJson;
            }
        }
        
        // 如果都找不到，返回原内容
        return trimmed;
    }

    private LocalDate safeParseDate(Object obj) {
        if (obj == null) return null;
        try {
            String str = obj.toString().trim();
            if (str.isEmpty()) return null;
            // 尝试匹配 YYYY-MM-DD
            if (str.length() >= 10) {
                return LocalDate.parse(str.substring(0, 10));
            }
            return null;
        } catch (Exception e) {
            log.warn("日期解析失败: {}", obj);
            return null;
        }
    }

    private Integer safeParseInt(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
            return Integer.parseInt(obj.toString().trim());
        } catch (Exception e) {
            log.warn("整数解析失败: {}", obj);
            return null;
        }
    }

    private BigDecimal safeParseBigDecimal(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return new BigDecimal(obj.toString());
            }
            return new BigDecimal(obj.toString().trim());
        } catch (Exception e) {
            log.warn("数字解析失败: {}", obj);
            return null;
        }
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


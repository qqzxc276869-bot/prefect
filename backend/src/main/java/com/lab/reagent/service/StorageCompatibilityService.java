package com.lab.reagent.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.*;
import com.lab.reagent.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 存储兼容性检查服务
 */
@Service
public class StorageCompatibilityService {
    
    @Autowired
    private StorageIncompatibilityRulesMapper incompatibilityRulesMapper;
    
    @Autowired
    private StorageLocationAttributesMapper locationAttributesMapper;
    
    @Autowired
    private StorageWarningLogMapper warningLogMapper;
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    @Autowired
    private ReagentMapper reagentMapper;
    
    /**
     * 检查存储兼容性
     * 
     * @param reagentId 要存储的试剂ID
     * @param locationId 目标库位ID
     * @return 检查结果，包含是否允许存储、警告信息等
     */
    public Map<String, Object> checkCompatibility(Long reagentId, Long locationId, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        String highestLevel = "LOW";
        boolean blocked = false;
        
        // 1. 获取试剂信息
        Reagent reagent = reagentMapper.selectById(reagentId);
        if (reagent == null) {
            result.put("success", false);
            result.put("message", "试剂不存在");
            return result;
        }
        
        String dangerLevel = reagent.getDangerLevel();
        
        // 2. 获取库位属性
        LambdaQueryWrapper<StorageLocationAttributes> attrWrapper = new LambdaQueryWrapper<>();
        attrWrapper.eq(StorageLocationAttributes::getLocationId, locationId);
        StorageLocationAttributes attributes = locationAttributesMapper.selectOne(attrWrapper);
        
        if (attributes != null) {
            // 检查禁止存放类型
            if (attributes.getForbiddenHazardTypes() != null && !attributes.getForbiddenHazardTypes().isEmpty()) {
                List<String> forbidden = JSON.parseArray(attributes.getForbiddenHazardTypes(), String.class);
                if (forbidden.contains(dangerLevel)) {
                    warnings.add("该库位禁止存放 " + dangerLevel + " 类型的试剂");
                    highestLevel = "HIGH";
                    blocked = true;
                }
            }
            
            // 检查允许存放类型
            if (attributes.getAllowedHazardTypes() != null && !attributes.getAllowedHazardTypes().isEmpty()) {
                List<String> allowed = JSON.parseArray(attributes.getAllowedHazardTypes(), String.class);
                if (!allowed.contains(dangerLevel) && !"普通试剂".equals(dangerLevel)) {
                    warnings.add("该库位未配置允许存放 " + dangerLevel + " 类型的试剂");
                    highestLevel = "MEDIUM";
                }
            }
        }
        
        // 3. 检查与该库位现有试剂的不兼容性
        LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
        invWrapper.eq(Inventory::getLocationId, locationId);
        List<Inventory> existingInventories = inventoryMapper.selectList(invWrapper);
        
        for (Inventory inv : existingInventories) {
            Reagent existingReagent = reagentMapper.selectById(inv.getReagentId());
            if (existingReagent != null) {
                String existingDangerLevel = existingReagent.getDangerLevel();
                
                // 检查不兼容规则
                LambdaQueryWrapper<StorageIncompatibilityRules> ruleWrapper = new LambdaQueryWrapper<>();
                ruleWrapper.eq(StorageIncompatibilityRules::getEnabled, 1)
                          .and(w -> w.and(ww -> ww.eq(StorageIncompatibilityRules::getHazardType1, dangerLevel)
                                                   .eq(StorageIncompatibilityRules::getHazardType2, existingDangerLevel))
                                     .or(ww -> ww.eq(StorageIncompatibilityRules::getHazardType1, existingDangerLevel)
                                                 .eq(StorageIncompatibilityRules::getHazardType2, dangerLevel)));
                
                List<StorageIncompatibilityRules> rules = incompatibilityRulesMapper.selectList(ruleWrapper);
                
                for (StorageIncompatibilityRules rule : rules) {
                    warnings.add(String.format("与现有试剂 [%s] 存在不兼容：%s", existingReagent.getName(), rule.getDescription()));
                    
                    if ("HIGH".equals(rule.getIncompatibilityLevel())) {
                        highestLevel = "HIGH";
                        blocked = true;
                    } else if ("MEDIUM".equals(rule.getIncompatibilityLevel()) && !"HIGH".equals(highestLevel)) {
                        highestLevel = "MEDIUM";
                    }
                }
            }
        }
        
        // 4. 记录警告日志
        if (!warnings.isEmpty()) {
            StorageWarningLog log = new StorageWarningLog();
            log.setReagentId(reagentId);
            log.setLocationId(locationId);
            log.setWarningType("INCOMPATIBILITY");
            log.setWarningLevel(highestLevel);
            log.setWarningMessage(String.join("; ", warnings));
            log.setOperatorId(operatorId);
            log.setOperatorName(operatorName);
            log.setActionTaken(blocked ? "BLOCKED" : "WARNING");
            log.setCreateTime(LocalDateTime.now());
            warningLogMapper.insert(log);
        }
        
        // 5. 返回结果
        result.put("success", !blocked);
        result.put("blocked", blocked);
        result.put("warnings", warnings);
        result.put("warningLevel", highestLevel);
        result.put("message", blocked ? "存储被阻止，存在高危不兼容" : (warnings.isEmpty() ? "存储兼容性检查通过" : "存在警告，请谨慎操作"));
        
        return result;
    }
}


package com.lab.reagent.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.ReagentLifecycle;
import com.lab.reagent.entity.ScanOperationLog;
import com.lab.reagent.mapper.ReagentLifecycleMapper;
import com.lab.reagent.mapper.ScanOperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 试剂生命周期服务
 */
@Service
public class ReagentLifecycleService {
    
    @Autowired
    private ReagentLifecycleMapper lifecycleMapper;
    
    @Autowired
    private ScanOperationLogMapper scanLogMapper;
    
    /**
     * 生成二维码
     */
    public String generateQrCode(Long inventoryId) {
        return "QR" + System.currentTimeMillis() + "_" + inventoryId;
    }
    
    /**
     * 创建生命周期记录
     */
    @Transactional
    public int create(ReagentLifecycle lifecycle) {
        if (lifecycle.getQrCode() == null || lifecycle.getQrCode().isEmpty()) {
            lifecycle.setQrCode(generateQrCode(lifecycle.getInventoryId()));
        }
        if (lifecycle.getPhysicalStatus() == null) {
            lifecycle.setPhysicalStatus("SEALED");
        }
        if (lifecycle.getSealIntegrity() == null) {
            lifecycle.setSealIntegrity("INTACT");
        }
        if (lifecycle.getUsageCount() == null) {
            lifecycle.setUsageCount(0);
        }
        return lifecycleMapper.insert(lifecycle);
    }
    
    /**
     * 根据二维码查询
     */
    public ReagentLifecycle getByQrCode(String qrCode) {
        LambdaQueryWrapper<ReagentLifecycle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReagentLifecycle::getQrCode, qrCode);
        return lifecycleMapper.selectOne(wrapper);
    }
    
    /**
     * 根据库存ID查询
     */
    public ReagentLifecycle getByInventoryId(Long inventoryId) {
        LambdaQueryWrapper<ReagentLifecycle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReagentLifecycle::getInventoryId, inventoryId);
        return lifecycleMapper.selectOne(wrapper);
    }
    
    /**
     * 标记为已开封
     */
    @Transactional
    public boolean markAsOpened(String qrCode, Long userId, String userName, Integer validityDays) {
        ReagentLifecycle lifecycle = getByQrCode(qrCode);
        if (lifecycle == null || !"SEALED".equals(lifecycle.getPhysicalStatus())) {
            return false;
        }
        
        lifecycle.setPhysicalStatus("OPENED");
        lifecycle.setOpenedDate(LocalDateTime.now());
        lifecycle.setOpenedById(userId);
        lifecycle.setOpenedByName(userName);
        
        if (validityDays != null && validityDays > 0) {
            lifecycle.setOpenedValidityDays(validityDays);
            lifecycle.setOpenedExpiryDate(LocalDate.now().plusDays(validityDays));
        }
        
        // 记录位置历史
        updateLocationHistory(lifecycle, lifecycle.getCurrentLocationId(), "开封");
        
        return lifecycleMapper.updateById(lifecycle) > 0;
    }
    
    /**
     * 更新使用记录
     */
    @Transactional
    public boolean recordUsage(String qrCode, BigDecimal usedQuantity) {
        ReagentLifecycle lifecycle = getByQrCode(qrCode);
        if (lifecycle == null) {
            return false;
        }
        
        lifecycle.setLastUsedDate(LocalDateTime.now());
        lifecycle.setUsageCount(lifecycle.getUsageCount() + 1);
        
        if (usedQuantity != null && lifecycle.getRemainingQuantity() != null) {
            BigDecimal remaining = lifecycle.getRemainingQuantity().subtract(usedQuantity);
            lifecycle.setRemainingQuantity(remaining);
            
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                lifecycle.setPhysicalStatus("EMPTY");
            } else {
                lifecycle.setPhysicalStatus("PARTIALLY_USED");
            }
        }
        
        return lifecycleMapper.updateById(lifecycle) > 0;
    }
    
    /**
     * 转移库位
     */
    @Transactional
    public boolean transferLocation(String qrCode, Long newLocationId, String reason) {
        ReagentLifecycle lifecycle = getByQrCode(qrCode);
        if (lifecycle == null) {
            return false;
        }
        
        Long oldLocationId = lifecycle.getCurrentLocationId();
        lifecycle.setCurrentLocationId(newLocationId);
        
        updateLocationHistory(lifecycle, newLocationId, reason);
        
        return lifecycleMapper.updateById(lifecycle) > 0;
    }
    
    /**
     * 更新位置历史
     */
    @SuppressWarnings("unchecked")
    private void updateLocationHistory(ReagentLifecycle lifecycle, Long locationId, String reason) {
        List<Map<String, Object>> history;
        
        if (lifecycle.getLocationHistory() != null && !lifecycle.getLocationHistory().isEmpty()) {
            history = (List<Map<String, Object>>) (List<?>) JSON.parseArray(lifecycle.getLocationHistory(), Map.class);
        } else {
            history = new ArrayList<>();
        }
        
        Map<String, Object> record = new HashMap<>();
        record.put("locationId", locationId);
        record.put("timestamp", LocalDateTime.now().toString());
        record.put("reason", reason);
        
        history.add(record);
        lifecycle.setLocationHistory(JSON.toJSONString(history));
    }
    
    /**
     * 记录扫码日志
     */
    public void logScan(String qrCode, String scanType, String operationType, Long userId, String userName, String result) {
        ScanOperationLog log = new ScanOperationLog();
        log.setQrCode(qrCode);
        log.setScanType(scanType);
        log.setOperationType(operationType);
        log.setUserId(userId);
        log.setUserName(userName);
        log.setResult(result);
        log.setCreateTime(LocalDateTime.now());
        scanLogMapper.insert(log);
    }
}


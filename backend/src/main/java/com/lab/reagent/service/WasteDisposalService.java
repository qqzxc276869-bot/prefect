package com.lab.reagent.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.WasteCategory;
import com.lab.reagent.entity.WasteDisposalRecord;
import com.lab.reagent.mapper.WasteCategoryMapper;
import com.lab.reagent.mapper.WasteDisposalRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 废弃物管理服务
 */
@Service
public class WasteDisposalService {
    
    @Autowired
    private WasteDisposalRecordMapper disposalRecordMapper;
    
    @Autowired
    private WasteCategoryMapper wasteCategoryMapper;
    
    /**
     * 分页查询废弃物记录
     */
    public Page<WasteDisposalRecord> page(Page<WasteDisposalRecord> page, String keyword, String status, Long groupId) {
        LambdaQueryWrapper<WasteDisposalRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(WasteDisposalRecord::getDisposalNo, keyword)
                   .or().like(WasteDisposalRecord::getMainComponents, keyword);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(WasteDisposalRecord::getDisposalStatus, status);
        }
        
        if (groupId != null) {
            wrapper.eq(WasteDisposalRecord::getGroupId, groupId);
        }
        
        wrapper.orderByDesc(WasteDisposalRecord::getCreateTime);
        
        return disposalRecordMapper.selectPage(page, wrapper);
    }
    
    /**
     * 创建废弃物登记
     */
    public int createRecord(WasteDisposalRecord record) {
        // 生成报废单号
        String disposalNo = "WD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        record.setDisposalNo(disposalNo);
        record.setDisposalStatus("REGISTERED");
        
        // 生成标签信息
        Map<String, Object> labelInfo = new HashMap<>();
        labelInfo.put("disposalNo", disposalNo);
        labelInfo.put("wasteType", record.getWasteType());
        labelInfo.put("mainComponents", record.getMainComponents());
        labelInfo.put("quantity", record.getQuantity());
        labelInfo.put("unit", record.getUnit());
        labelInfo.put("registeredDate", LocalDateTime.now().toString());
        labelInfo.put("registeredBy", record.getApplicantName());
        
        // 获取废弃物类别信息
        if (record.getWasteCategoryId() != null) {
            WasteCategory category = wasteCategoryMapper.selectById(record.getWasteCategoryId());
            if (category != null) {
                labelInfo.put("categoryName", category.getCategoryName());
                labelInfo.put("hazardLevel", category.getHazardLevel());
                labelInfo.put("labelColor", category.getLabelColor());
            }
        }
        
        record.setLabelInfo(JSON.toJSONString(labelInfo));
        record.setLabelPrinted(0);
        
        return disposalRecordMapper.insert(record);
    }
    
    /**
     * 更新废弃物状态
     */
    public int updateStatus(Long id, String status, String remark) {
        WasteDisposalRecord record = disposalRecordMapper.selectById(id);
        if (record == null) {
            return 0;
        }
        
        record.setDisposalStatus(status);
        
        if (remark != null && !remark.isEmpty()) {
            String currentRemark = record.getRemark();
            record.setRemark((currentRemark != null ? currentRemark + "; " : "") + remark);
        }
        
        return disposalRecordMapper.updateById(record);
    }
    
    /**
     * 获取所有废弃物类别
     */
    public List<WasteCategory> getAllCategories() {
        return wasteCategoryMapper.selectList(null);
    }
    
    /**
     * 根据ID获取废弃物记录
     */
    public WasteDisposalRecord getById(Long id) {
        return disposalRecordMapper.selectById(id);
    }
    
    /**
     * 生成废弃物标签（返回标签数据）
     */
    public Map<String, Object> generateLabel(Long id) {
        WasteDisposalRecord record = disposalRecordMapper.selectById(id);
        if (record == null || record.getLabelInfo() == null) {
            return null;
        }
        
        // 标记为已打印
        record.setLabelPrinted(1);
        disposalRecordMapper.updateById(record);
        
        return JSON.parseObject(record.getLabelInfo(), Map.class);
    }
}


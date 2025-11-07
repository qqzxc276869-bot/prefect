package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.SopDocument;
import com.lab.reagent.entity.SopTrainingRecord;
import com.lab.reagent.mapper.SopDocumentMapper;
import com.lab.reagent.mapper.SopTrainingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SOP 服务
 */
@Service
public class SopService {
    
    @Autowired
    private SopDocumentMapper sopDocumentMapper;
    
    @Autowired
    private SopTrainingRecordMapper trainingRecordMapper;
    
    /**
     * 分页查询SOP
     */
    public Page<SopDocument> page(Page<SopDocument> page, String keyword, String status) {
        LambdaQueryWrapper<SopDocument> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(SopDocument::getTitle, keyword)
                   .or().like(SopDocument::getSopNo, keyword)
                   .or().like(SopDocument::getCategory, keyword);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(SopDocument::getStatus, status);
        }
        
        wrapper.orderByDesc(SopDocument::getCreateTime);
        
        return sopDocumentMapper.selectPage(page, wrapper);
    }
    
    /**
     * 保存或更新SOP
     */
    public int saveOrUpdate(SopDocument sopDocument) {
        if (sopDocument.getId() != null) {
            return sopDocumentMapper.updateById(sopDocument);
        } else {
            return sopDocumentMapper.insert(sopDocument);
        }
    }
    
    /**
     * 根据ID获取SOP
     */
    public SopDocument getById(Long id) {
        return sopDocumentMapper.selectById(id);
    }
    
    /**
     * 删除SOP
     */
    public int delete(Long id) {
        return sopDocumentMapper.deleteById(id);
    }
    
    /**
     * 检查用户是否已完成SOP培训
     */
    public boolean hasCompletedTraining(Long userId, Long sopId) {
        LambdaQueryWrapper<SopTrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SopTrainingRecord::getUserId, userId)
               .eq(SopTrainingRecord::getSopId, sopId)
               .eq(SopTrainingRecord::getPassed, 1)
               .ge(SopTrainingRecord::getValidUntil, LocalDate.now());
        
        return trainingRecordMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 记录培训
     */
    public int recordTraining(SopTrainingRecord record) {
        return trainingRecordMapper.insert(record);
    }
    
    /**
     * 获取用户的培训记录
     */
    public List<SopTrainingRecord> getUserTrainingRecords(Long userId) {
        LambdaQueryWrapper<SopTrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SopTrainingRecord::getUserId, userId)
               .orderByDesc(SopTrainingRecord::getCreateTime);
        return trainingRecordMapper.selectList(wrapper);
    }
}


package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.GhsMsds;
import com.lab.reagent.mapper.GhsMsdsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * GHS/MSDS 服务
 */
@Service
public class GhsMsdsService {
    
    @Autowired
    private GhsMsdsMapper ghsMsdsMapper;
    
    /**
     * 根据试剂ID获取MSDS
     */
    public GhsMsds getByReagentId(Long reagentId) {
        LambdaQueryWrapper<GhsMsds> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhsMsds::getReagentId, reagentId);
        return ghsMsdsMapper.selectOne(wrapper);
    }
    
    /**
     * 保存或更新MSDS
     */
    public int saveOrUpdate(GhsMsds ghsMsds) {
        if (ghsMsds.getId() != null) {
            return ghsMsdsMapper.updateById(ghsMsds);
        } else {
            return ghsMsdsMapper.insert(ghsMsds);
        }
    }
    
    /**
     * 分页查询
     */
    public Page<GhsMsds> page(Page<GhsMsds> page, String keyword) {
        LambdaQueryWrapper<GhsMsds> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(GhsMsds::getSignalWord, keyword)
                   .or().like(GhsMsds::getHazardClass, keyword);
        }
        return ghsMsdsMapper.selectPage(page, wrapper);
    }
    
    /**
     * 删除
     */
    public int delete(Long id) {
        return ghsMsdsMapper.deleteById(id);
    }
    
    /**
     * 获取未验证的MSDS列表
     */
    public List<GhsMsds> getUnverifiedList() {
        LambdaQueryWrapper<GhsMsds> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhsMsds::getVerified, 0);
        return ghsMsdsMapper.selectList(wrapper);
    }
}


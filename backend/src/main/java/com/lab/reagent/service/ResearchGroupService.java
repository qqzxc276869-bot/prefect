package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.ResearchGroup;
import com.lab.reagent.entity.ResearchGroupMember;
import com.lab.reagent.mapper.ResearchGroupMapper;
import com.lab.reagent.mapper.ResearchGroupMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课题组服务
 */
@Service
public class ResearchGroupService {
    
    @Autowired
    private ResearchGroupMapper researchGroupMapper;
    
    @Autowired
    private ResearchGroupMemberMapper memberMapper;
    
    /**
     * 分页查询课题组
     */
    public Page<ResearchGroup> page(Page<ResearchGroup> page, String keyword) {
        LambdaQueryWrapper<ResearchGroup> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ResearchGroup::getGroupName, keyword)
                   .or().like(ResearchGroup::getGroupCode, keyword)
                   .or().like(ResearchGroup::getPiName, keyword);
        }
        
        wrapper.orderByDesc(ResearchGroup::getCreateTime);
        
        return researchGroupMapper.selectPage(page, wrapper);
    }
    
    /**
     * 保存或更新课题组
     */
    @Transactional
    public int saveOrUpdate(ResearchGroup group) {
        if (group.getAvailableBudget() == null && group.getTotalBudget() != null) {
            group.setAvailableBudget(group.getTotalBudget());
        }
        if (group.getUsedBudget() == null) {
            group.setUsedBudget(BigDecimal.ZERO);
        }
        
        if (group.getId() != null) {
            return researchGroupMapper.updateById(group);
        } else {
            return researchGroupMapper.insert(group);
        }
    }
    
    /**
     * 根据ID获取课题组
     */
    public ResearchGroup getById(Long id) {
        return researchGroupMapper.selectById(id);
    }
    
    /**
     * 删除课题组
     */
    public int delete(Long id) {
        return researchGroupMapper.deleteById(id);
    }
    
    /**
     * 添加课题组成员
     */
    public int addMember(ResearchGroupMember member) {
        return memberMapper.insert(member);
    }
    
    /**
     * 获取课题组成员列表
     */
    public List<ResearchGroupMember> getMembers(Long groupId) {
        LambdaQueryWrapper<ResearchGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearchGroupMember::getGroupId, groupId)
               .eq(ResearchGroupMember::getStatus, "ACTIVE")
               .orderByDesc(ResearchGroupMember::getJoinDate);
        return memberMapper.selectList(wrapper);
    }
    
    /**
     * 移除成员
     */
    @Transactional
    public int removeMember(Long id) {
        ResearchGroupMember member = memberMapper.selectById(id);
        if (member != null) {
            member.setStatus("LEFT");
            return memberMapper.updateById(member);
        }
        return 0;
    }
    
    /**
     * 更新预算
     */
    @Transactional
    public boolean updateBudget(Long groupId, BigDecimal amount, boolean isUsage) {
        ResearchGroup group = researchGroupMapper.selectById(groupId);
        if (group == null) {
            return false;
        }
        
        if (isUsage) {
            // 使用预算
            BigDecimal available = group.getAvailableBudget();
            if (available.compareTo(amount) < 0) {
                return false;  // 预算不足
            }
            group.setUsedBudget(group.getUsedBudget().add(amount));
            group.setAvailableBudget(available.subtract(amount));
        } else {
            // 退还预算
            group.setUsedBudget(group.getUsedBudget().subtract(amount));
            group.setAvailableBudget(group.getAvailableBudget().add(amount));
        }
        
        return researchGroupMapper.updateById(group) > 0;
    }
}


package com.lab.reagent.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.reagent.entity.Application;
import com.lab.reagent.entity.Reagent;
import com.lab.reagent.entity.SysUser;
import com.lab.reagent.mapper.ApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请Service
 */
@Service
public class ApplicationService extends ServiceImpl<ApplicationMapper, Application> {
    
    @Autowired
    private ReagentService reagentService;
    
    @Autowired
    private SysUserService sysUserService;
    
    /**
     * 提交申请
     */
    public void submitApplication(Application application, Long applicantId) {
        // 获取申请人信息
        SysUser applicant = sysUserService.getById(applicantId);
        if (applicant == null) {
            throw new RuntimeException("申请人不存在");
        }
        
        // 获取试剂信息
        Reagent reagent = reagentService.getById(application.getReagentId());
        if (reagent == null) {
            throw new RuntimeException("试剂不存在");
        }
        
        // 生成申请单号
        application.setApplicationNo("APP" + IdUtil.getSnowflakeNextIdStr());
        application.setReagentName(reagent.getName());
        application.setApplicantId(applicantId);
        application.setApplicantName(applicant.getRealName());
        application.setStatus("PENDING");
        
        this.save(application);
    }
    
    /**
     * 审批申请
     */
    public void reviewApplication(Long id, String status, String remark, Long reviewerId) {
        Application application = this.getById(id);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }
        
        if (!"PENDING".equals(application.getStatus())) {
            throw new RuntimeException("申请已处理，无法重复审批");
        }
        
        // 获取审批人信息
        SysUser reviewer = sysUserService.getById(reviewerId);
        if (reviewer == null) {
            throw new RuntimeException("审批人不存在");
        }
        
        application.setStatus(status);
        application.setReviewerId(reviewerId);
        application.setReviewerName(reviewer.getRealName());
        application.setReviewTime(LocalDateTime.now());
        application.setReviewRemark(remark);
        
        this.updateById(application);
    }
    
    /**
     * 查询用户申请列表
     */
    public List<Application> getUserApplications(Long userId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getApplicantId, userId);
        wrapper.orderByDesc(Application::getCreateTime);
        return this.list(wrapper);
    }
    
    /**
     * 查询待审批申请列表
     */
    public List<Application> getPendingApplications() {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getStatus, "PENDING");
        wrapper.orderByAsc(Application::getCreateTime);
        return this.list(wrapper);
    }
}








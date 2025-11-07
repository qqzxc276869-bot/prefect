package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.BudgetTransaction;
import com.lab.reagent.entity.ProcurementRequest;
import com.lab.reagent.entity.ResearchGroup;
import com.lab.reagent.mapper.BudgetTransactionMapper;
import com.lab.reagent.mapper.ProcurementRequestMapper;
import com.lab.reagent.mapper.ResearchGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 采购服务
 */
@Service
public class ProcurementService {
    
    @Autowired
    private ProcurementRequestMapper procurementRequestMapper;
    
    @Autowired
    private BudgetTransactionMapper budgetTransactionMapper;
    
    @Autowired
    private ResearchGroupMapper researchGroupMapper;
    
    @Autowired
    private ResearchGroupService researchGroupService;
    
    /**
     * 分页查询采购申请
     */
    public Page<ProcurementRequest> page(Page<ProcurementRequest> page, String keyword, String status, Long groupId) {
        LambdaQueryWrapper<ProcurementRequest> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ProcurementRequest::getRequestNo, keyword)
                   .or().like(ProcurementRequest::getReagentName, keyword);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(ProcurementRequest::getStatus, status);
        }
        
        if (groupId != null) {
            wrapper.eq(ProcurementRequest::getGroupId, groupId);
        }
        
        wrapper.orderByDesc(ProcurementRequest::getCreateTime);
        
        return procurementRequestMapper.selectPage(page, wrapper);
    }
    
    /**
     * 创建采购申请
     */
    @Transactional
    public int createRequest(ProcurementRequest request) {
        // 生成申请单号
        String requestNo = "PR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        request.setRequestNo(requestNo);
        request.setStatus("PENDING");
        
        // 计算预估总价
        if (request.getEstimatedPrice() != null && request.getQuantity() != null) {
            request.setEstimatedTotal(request.getEstimatedPrice().multiply(request.getQuantity()));
        }
        
        return procurementRequestMapper.insert(request);
    }
    
    /**
     * PI审批
     */
    @Transactional
    public boolean piApprove(Long id, Long reviewerId, String reviewerName, String remark, boolean approved) {
        ProcurementRequest request = procurementRequestMapper.selectById(id);
        if (request == null || !"PENDING".equals(request.getStatus())) {
            return false;
        }
        
        request.setPiReviewerId(reviewerId);
        request.setPiReviewerName(reviewerName);
        request.setPiReviewTime(LocalDateTime.now());
        request.setPiReviewRemark(remark);
        
        if (approved) {
            // 检查预算
            ResearchGroup group = researchGroupMapper.selectById(request.getGroupId());
            if (group != null && request.getEstimatedTotal() != null) {
                if (group.getAvailableBudget().compareTo(request.getEstimatedTotal()) < 0) {
                    return false;  // 预算不足
                }
            }
            request.setStatus("PI_APPROVED");
        } else {
            request.setStatus("REJECTED");
        }
        
        return procurementRequestMapper.updateById(request) > 0;
    }
    
    /**
     * 管理员审批（下单）
     */
    @Transactional
    public boolean adminApprove(Long id, Long reviewerId, String reviewerName, String remark, 
                                BigDecimal actualPrice, String supplier) {
        ProcurementRequest request = procurementRequestMapper.selectById(id);
        if (request == null || !"PI_APPROVED".equals(request.getStatus())) {
            return false;
        }
        
        request.setAdminReviewerId(reviewerId);
        request.setAdminReviewerName(reviewerName);
        request.setAdminReviewTime(LocalDateTime.now());
        request.setAdminReviewRemark(remark);
        request.setActualPrice(actualPrice);
        request.setActualTotal(actualPrice.multiply(request.getQuantity()));
        request.setSupplier(supplier);
        request.setStatus("ORDERED");
        
        // 扣减预算
        boolean budgetUpdated = researchGroupService.updateBudget(
            request.getGroupId(), 
            request.getActualTotal(), 
            true
        );
        
        if (!budgetUpdated) {
            return false;
        }
        
        // 记录预算交易
        ResearchGroup group = researchGroupMapper.selectById(request.getGroupId());
        BudgetTransaction transaction = new BudgetTransaction();
        transaction.setGroupId(request.getGroupId());
        transaction.setTransactionType("PROCUREMENT");
        transaction.setAmount(request.getActualTotal());
        transaction.setRelatedId(id);
        transaction.setRelatedType("PROCUREMENT");
        transaction.setBalanceBefore(group.getAvailableBudget().add(request.getActualTotal()));
        transaction.setBalanceAfter(group.getAvailableBudget());
        transaction.setReagentName(request.getReagentName());
        transaction.setQuantity(request.getQuantity());
        transaction.setDescription("采购申请：" + request.getRequestNo());
        transaction.setOperatorId(reviewerId);
        transaction.setOperatorName(reviewerName);
        budgetTransactionMapper.insert(transaction);
        
        return procurementRequestMapper.updateById(request) > 0;
    }
    
    /**
     * 根据ID获取采购申请
     */
    public ProcurementRequest getById(Long id) {
        return procurementRequestMapper.selectById(id);
    }
}


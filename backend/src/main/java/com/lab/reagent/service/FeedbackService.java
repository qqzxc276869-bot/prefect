package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.Feedback;
import com.lab.reagent.mapper.FeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈Service
 */
@Service
public class FeedbackService {
    
    @Autowired
    private FeedbackMapper feedbackMapper;
    
    /**
     * 提交反馈
     */
    public void submitFeedback(Feedback feedback) {
        feedback.setStatus("PENDING");
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
    }
    
    /**
     * 查询我的反馈
     */
    public List<Feedback> getMyFeedback(Long userId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId);
        wrapper.orderByDesc(Feedback::getCreateTime);
        return feedbackMapper.selectList(wrapper);
    }
    
    /**
     * 查询所有反馈
     */
    public List<Feedback> getAllFeedback() {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Feedback::getCreateTime);
        return feedbackMapper.selectList(wrapper);
    }
    
    /**
     * 查询待处理反馈
     */
    public List<Feedback> getPendingFeedback() {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getStatus, "PENDING");
        wrapper.orderByDesc(Feedback::getCreateTime);
        return feedbackMapper.selectList(wrapper);
    }
    
    /**
     * 处理反馈
     */
    public void handleFeedback(Long id, String status, String remark, Long handlerId, String handlerName) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback != null) {
            feedback.setStatus(status);
            feedback.setHandleRemark(remark);
            feedback.setHandlerId(handlerId);
            feedback.setHandlerName(handlerName);
            feedback.setHandleTime(LocalDateTime.now());
            feedbackMapper.updateById(feedback);
        }
    }
}





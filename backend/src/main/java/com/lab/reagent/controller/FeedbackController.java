package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.entity.Feedback;
import com.lab.reagent.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 反馈Controller
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
    @Autowired
    private FeedbackService feedbackService;
    
    /**
     * 提交反馈
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Feedback feedback,
                                  @RequestHeader("userId") Long userId,
                                  @RequestHeader("realName") String realName) {
        try {
            String decodedName = URLDecoder.decode(realName, StandardCharsets.UTF_8.name());
            feedback.setUserId(userId);
            feedback.setUserName(decodedName);
            feedbackService.submitFeedback(feedback);
            return Result.success("反馈提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询我的反馈
     */
    @GetMapping("/my")
    public Result<List<Feedback>> my(@RequestHeader("userId") Long userId) {
        try {
            List<Feedback> list = feedbackService.getMyFeedback(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询所有反馈（仓管员）
     */
    @GetMapping("/all")
    public Result<List<Feedback>> all() {
        try {
            List<Feedback> list = feedbackService.getAllFeedback();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询待处理反馈
     */
    @GetMapping("/pending")
    public Result<List<Feedback>> pending() {
        try {
            List<Feedback> list = feedbackService.getPendingFeedback();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 处理反馈
     */
    @PostMapping("/handle/{id}")
    public Result<String> handle(@PathVariable Long id,
                                  @RequestBody Map<String, String> params,
                                  @RequestHeader("userId") Long userId,
                                  @RequestHeader("realName") String realName) {
        try {
            String decodedName = URLDecoder.decode(realName, StandardCharsets.UTF_8.name());
            String status = params.get("status");
            String remark = params.get("remark");
            feedbackService.handleFeedback(id, status, remark, userId, decodedName);
            return Result.success("处理成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}





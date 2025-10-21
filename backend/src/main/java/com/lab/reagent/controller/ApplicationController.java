package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.entity.Application;
import com.lab.reagent.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 申请Controller
 */
@RestController
@RequestMapping("/api/application")
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;
    
    /**
     * 提交申请
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Application application, 
                                  @RequestHeader("userId") Long userId) {
        try {
            applicationService.submitApplication(application, userId);
            return Result.success("提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询我的申请
     */
    @GetMapping("/my")
    public Result<List<Application>> myApplications(@RequestHeader("userId") Long userId) {
        try {
            List<Application> list = applicationService.getUserApplications(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询待审批申请
     */
    @GetMapping("/pending")
    public Result<List<Application>> pending() {
        try {
            List<Application> list = applicationService.getPendingApplications();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询所有申请
     */
    @GetMapping("/all")
    public Result<List<Application>> all() {
        try {
            List<Application> list = applicationService.list();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 审批申请
     */
    @PostMapping("/review/{id}")
    public Result<String> review(@PathVariable Long id, 
                                  @RequestBody Map<String, Object> params,
                                  @RequestHeader("userId") Long reviewerId) {
        try {
            String status = (String) params.get("status");
            String remark = (String) params.get("remark");
            
            applicationService.reviewApplication(id, status, remark, reviewerId);
            return Result.success("审批成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}







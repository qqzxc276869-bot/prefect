package com.lab.reagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.SopDocument;
import com.lab.reagent.entity.SopTrainingRecord;
import com.lab.reagent.service.SopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SOP Controller
 */
@RestController
@RequestMapping("/sop")
@CrossOrigin
public class SopController {
    
    @Autowired
    private SopService sopService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) String status) {
        Page<SopDocument> page = new Page<>(current, size);
        page = sopService.page(page, keyword, status);
        return Result.success(page);
    }
    
    /**
     * 根据ID获取
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        SopDocument sop = sopService.getById(id);
        return Result.success(sop);
    }
    
    /**
     * 保存或更新
     */
    @PostMapping("/save")
    public Result save(@RequestBody SopDocument sopDocument) {
        int result = sopService.saveOrUpdate(sopDocument);
        return result > 0 ? Result.success("保存成功") : Result.error("保存失败");
    }
    
    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        int result = sopService.delete(id);
        return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 检查培训状态
     */
    @GetMapping("/training/check")
    public Result checkTraining(@RequestParam Long userId, @RequestParam Long sopId) {
        boolean completed = sopService.hasCompletedTraining(userId, sopId);
        return Result.success(completed);
    }
    
    /**
     * 记录培训
     */
    @PostMapping("/training/record")
    public Result recordTraining(@RequestBody SopTrainingRecord record) {
        int result = sopService.recordTraining(record);
        return result > 0 ? Result.success("培训记录保存成功") : Result.error("保存失败");
    }
    
    /**
     * 获取用户培训记录
     */
    @GetMapping("/training/user/{userId}")
    public Result getUserTraining(@PathVariable Long userId) {
        List<SopTrainingRecord> records = sopService.getUserTrainingRecords(userId);
        return Result.success(records);
    }
}


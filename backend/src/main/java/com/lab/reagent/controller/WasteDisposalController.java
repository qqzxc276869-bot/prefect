package com.lab.reagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.WasteCategory;
import com.lab.reagent.entity.WasteDisposalRecord;
import com.lab.reagent.service.WasteDisposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 废弃物管理 Controller
 */
@RestController
@RequestMapping("/waste")
@CrossOrigin
public class WasteDisposalController {
    
    @Autowired
    private WasteDisposalService wasteDisposalService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) String status,
                      @RequestParam(required = false) Long groupId) {
        Page<WasteDisposalRecord> page = new Page<>(current, size);
        page = wasteDisposalService.page(page, keyword, status, groupId);
        return Result.success(page);
    }
    
    /**
     * 创建废弃物登记
     */
    @PostMapping("/create")
    public Result create(@RequestBody WasteDisposalRecord record) {
        int result = wasteDisposalService.createRecord(record);
        return result > 0 ? Result.success("登记成功") : Result.error("登记失败");
    }
    
    /**
     * 更新状态
     */
    @PostMapping("/update-status")
    public Result updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String status = params.get("status").toString();
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        
        int result = wasteDisposalService.updateStatus(id, status, remark);
        return result > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 获取所有废弃物类别
     */
    @GetMapping("/categories")
    public Result getCategories() {
        List<WasteCategory> categories = wasteDisposalService.getAllCategories();
        return Result.success(categories);
    }
    
    /**
     * 根据ID获取
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        WasteDisposalRecord record = wasteDisposalService.getById(id);
        return Result.success(record);
    }
    
    /**
     * 生成标签
     */
    @GetMapping("/label/{id}")
    public Result generateLabel(@PathVariable Long id) {
        Map<String, Object> label = wasteDisposalService.generateLabel(id);
        return label != null ? Result.success(label) : Result.error("生成失败");
    }
}


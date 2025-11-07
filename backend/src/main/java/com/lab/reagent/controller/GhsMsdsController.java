package com.lab.reagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.GhsMsds;
import com.lab.reagent.service.GhsMsdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * GHS/MSDS Controller
 */
@RestController
@RequestMapping("/ghs")
@CrossOrigin
public class GhsMsdsController {
    
    @Autowired
    private GhsMsdsService ghsMsdsService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String keyword) {
        Page<GhsMsds> page = new Page<>(current, size);
        page = ghsMsdsService.page(page, keyword);
        return Result.success(page);
    }
    
    /**
     * 根据试剂ID获取
     */
    @GetMapping("/reagent/{reagentId}")
    public Result getByReagentId(@PathVariable Long reagentId) {
        GhsMsds msds = ghsMsdsService.getByReagentId(reagentId);
        return Result.success(msds);
    }
    
    /**
     * 保存或更新
     */
    @PostMapping("/save")
    public Result save(@RequestBody GhsMsds ghsMsds) {
        int result = ghsMsdsService.saveOrUpdate(ghsMsds);
        return result > 0 ? Result.success("保存成功") : Result.error("保存失败");
    }
    
    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        int result = ghsMsdsService.delete(id);
        return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 获取未验证列表
     */
    @GetMapping("/unverified")
    public Result getUnverified() {
        return Result.success(ghsMsdsService.getUnverifiedList());
    }
}


package com.lab.reagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.Reagent;
import com.lab.reagent.service.ReagentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 试剂Controller
 */
@RestController
@RequestMapping("/api/reagent")
public class ReagentController {
    
    @Autowired
    private ReagentService reagentService;
    
    /**
     * 查询试剂列表
     */
    @GetMapping("/list")
    public Result<List<Reagent>> list(@RequestParam(required = false) String name) {
        try {
            LambdaQueryWrapper<Reagent> wrapper = new LambdaQueryWrapper<>();
            if (name != null && !name.isEmpty()) {
                wrapper.like(Reagent::getName, name);
            }
            wrapper.orderByDesc(Reagent::getCreateTime);
            
            List<Reagent> list = reagentService.list(wrapper);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加试剂
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Reagent reagent) {
        try {
            reagentService.save(reagent);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新试剂
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Reagent reagent) {
        try {
            reagentService.updateById(reagent);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除试剂
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            reagentService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}







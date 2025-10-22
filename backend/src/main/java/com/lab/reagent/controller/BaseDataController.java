package com.lab.reagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.ReagentCategory;
import com.lab.reagent.entity.StorageLocation;
import com.lab.reagent.mapper.ReagentCategoryMapper;
import com.lab.reagent.mapper.StorageLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础数据Controller
 */
@RestController
@RequestMapping("/api/base")
public class BaseDataController {
    
    @Autowired
    private ReagentCategoryMapper categoryMapper;
    
    @Autowired
    private StorageLocationMapper locationMapper;
    
    /**
     * 查询分类列表
     */
    @GetMapping("/category/list")
    public Result<List<ReagentCategory>> categoryList() {
        try {
            List<ReagentCategory> list = categoryMapper.selectList(new LambdaQueryWrapper<>());
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加分类
     */
    @PostMapping("/category/add")
    public Result<String> addCategory(@RequestBody ReagentCategory category) {
        try {
            categoryMapper.insert(category);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/category/delete/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        try {
            categoryMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询存放位置列表
     */
    @GetMapping("/location/list")
    public Result<List<StorageLocation>> locationList() {
        try {
            List<StorageLocation> list = locationMapper.selectList(new LambdaQueryWrapper<>());
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加存放位置
     */
    @PostMapping("/location/add")
    public Result<String> addLocation(@RequestBody StorageLocation location) {
        try {
            locationMapper.insert(location);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除存放位置
     */
    @DeleteMapping("/location/delete/{id}")
    public Result<String> deleteLocation(@PathVariable Long id) {
        try {
            locationMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}








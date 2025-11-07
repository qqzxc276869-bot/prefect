package com.lab.reagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.ResearchGroup;
import com.lab.reagent.entity.ResearchGroupMember;
import com.lab.reagent.service.ResearchGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课题组 Controller
 */
@RestController
@RequestMapping("/research-group")
@CrossOrigin
public class ResearchGroupController {
    
    @Autowired
    private ResearchGroupService researchGroupService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String keyword) {
        Page<ResearchGroup> page = new Page<>(current, size);
        page = researchGroupService.page(page, keyword);
        return Result.success(page);
    }
    
    /**
     * 根据ID获取
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        ResearchGroup group = researchGroupService.getById(id);
        return Result.success(group);
    }
    
    /**
     * 保存或更新
     */
    @PostMapping("/save")
    public Result save(@RequestBody ResearchGroup researchGroup) {
        int result = researchGroupService.saveOrUpdate(researchGroup);
        return result > 0 ? Result.success("保存成功") : Result.error("保存失败");
    }
    
    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        int result = researchGroupService.delete(id);
        return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 添加成员
     */
    @PostMapping("/member/add")
    public Result addMember(@RequestBody ResearchGroupMember member) {
        int result = researchGroupService.addMember(member);
        return result > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }
    
    /**
     * 获取成员列表
     */
    @GetMapping("/member/list/{groupId}")
    public Result getMembers(@PathVariable Long groupId) {
        List<ResearchGroupMember> members = researchGroupService.getMembers(groupId);
        return Result.success(members);
    }
    
    /**
     * 移除成员
     */
    @DeleteMapping("/member/{id}")
    public Result removeMember(@PathVariable Long id) {
        int result = researchGroupService.removeMember(id);
        return result > 0 ? Result.success("移除成功") : Result.error("移除失败");
    }
}


package com.lab.reagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.SysUser;
import com.lab.reagent.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理Controller
 */
@RestController
@RequestMapping("/api/user")
public class SysUserController {
    
    @Autowired
    private SysUserService sysUserService;
    
    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public Result<List<SysUser>> list(@RequestParam(required = false) String role) {
        try {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            if (role != null && !role.isEmpty()) {
                wrapper.eq(SysUser::getRole, role);
            }
            wrapper.orderByDesc(SysUser::getCreateTime);
            
            List<SysUser> list = sysUserService.list(wrapper);
            // 清除密码
            list.forEach(user -> user.setPassword(null));
            
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加用户
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysUser user) {
        try {
            sysUserService.addUser(user);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody SysUser user) {
        try {
            // 不允许更新密码
            user.setPassword(null);
            sysUserService.updateUser(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 启用/禁用用户
     */
    @PutMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        try {
            sysUserService.updateUserStatus(id, status);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            sysUserService.deleteUser(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}






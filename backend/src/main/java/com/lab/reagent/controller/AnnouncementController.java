package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.entity.Announcement;
import com.lab.reagent.service.AnnouncementService;
import com.lab.reagent.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/announcement")
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取公告列表（所有用户都可以查看）
     */
    @GetMapping
    public Result list(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @RequestParam(required = false) String role) {
        try {
            // 从token中获取用户角色，用于过滤公告
            String userRole = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                try {
                    String token = authorization.substring(7);
                    userRole = jwtUtil.getRoleFromToken(token);
                } catch (Exception e) {
                    log.warn("获取用户角色失败，将返回所有公告", e);
                }
            }
            // 如果请求参数中没有指定role，则使用用户角色
            if (role == null && userRole != null) {
                role = userRole;
            }
            List<Announcement> list = announcementService.listForRole(role);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取公告列表失败", e);
            return Result.error("获取公告列表失败：" + e.getMessage());
        }
    }

    /**
     * 发布公告（仅系统管理员）
     */
    @PostMapping
    public Result publish(@RequestHeader(value = "Authorization", required = false) String authorization,
                         @RequestBody Announcement announcement) {
        // 验证权限：只有ADMIN可以发布公告
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error("未授权：缺少令牌");
        }
        String token = authorization.substring(7);
        String userRole = jwtUtil.getRoleFromToken(token);
        if (userRole == null) {
            return Result.error("未授权：令牌无效");
        }
        if (!"ADMIN".equals(userRole)) {
            return Result.error("权限不足：只有系统管理员可以发布公告");
        }
        
        if (announcement == null || announcement.getTitle() == null || announcement.getContent() == null) {
            return Result.error("公告标题与内容不能为空");
        }
        boolean saved = announcementService.publish(announcement);
        return saved ? Result.success("公告发布成功") : Result.error("公告发布失败");
    }

    /**
     * 删除公告（仅系统管理员）
     */
    @DeleteMapping("/{id}")
    public Result delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                        @PathVariable Long id) {
        // 验证权限：只有ADMIN可以删除公告
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error("未授权：缺少令牌");
        }
        String token = authorization.substring(7);
        String userRole = jwtUtil.getRoleFromToken(token);
        if (userRole == null) {
            return Result.error("未授权：令牌无效");
        }
        if (!"ADMIN".equals(userRole)) {
            return Result.error("权限不足：只有系统管理员可以删除公告");
        }
        
        boolean removed = announcementService.removeById(id);
        return removed ? Result.success("删除成功") : Result.error("删除失败");
    }
}



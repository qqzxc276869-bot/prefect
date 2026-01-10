package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.reagent.entity.Announcement;
import com.lab.reagent.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {

    /**
     * 根据角色筛选公告
     * 规则：
     * - ADMIN可以看到所有公告
     * - TEACHER只能看到audience为ALL或TEACHER的公告（看不到STUDENT的公告）
     * - STUDENT只能看到audience为ALL或STUDENT的公告（看不到TEACHER的公告）
     */
    public List<Announcement> listForRole(String role) {
        try {
            LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
            
            // 根据角色筛选
            if (role != null && !role.trim().isEmpty()) {
                // ADMIN可以看到所有公告
                if ("ADMIN".equals(role)) {
                    // 不添加任何筛选条件，返回所有公告
                } else {
                    // TEACHER和STUDENT只能看到audience为ALL或匹配自己角色的公告
                    // 这样确保：TEACHER看不到STUDENT的公告，STUDENT看不到TEACHER的公告
                    wrapper.and(w -> w.eq(Announcement::getAudience, "ALL")
                            .or().eq(Announcement::getAudience, role));
                }
            } else {
                // 如果role为空，返回所有公告（用于管理员查看）
            }
            
            // 按发布时间降序，然后按创建时间降序
            wrapper.orderByDesc(Announcement::getPublishTime);
            wrapper.orderByDesc(Announcement::getCreateTime);
            
            return this.list(wrapper);
        } catch (Exception e) {
            // 如果查询失败，记录错误并返回空列表
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 发布公告
     */
    public boolean publish(Announcement announcement) {
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        if (announcement.getAudience() == null) {
            announcement.setAudience("ALL");
        }
        if (announcement.getPriority() == null) {
            announcement.setPriority("INFO");
        }
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        return this.save(announcement);
    }
}



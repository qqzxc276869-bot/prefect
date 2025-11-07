package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课题组成员实体
 */
@Data
@TableName("research_group_member")
public class ResearchGroupMember implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long groupId;
    
    private Long userId;
    
    private String userName;
    
    private String role;
    
    private LocalDate joinDate;
    
    private String status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}


package com.lab.reagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String realName;
    
    private String role;  // STUDENT, TEACHER, ADMIN
    
    private String email;
    
    private String phone;
    
    private String department;
    
    private Integer status;  // 1-启用, 0-禁用
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}







package com.lab.reagent.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {
    
    private String token;
    
    private Long userId;
    
    private String username;
    
    private String realName;
    
    private String role;
    
    private String department;
}







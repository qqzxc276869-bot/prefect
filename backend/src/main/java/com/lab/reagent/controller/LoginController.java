package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.dto.LoginDTO;
import com.lab.reagent.service.SysUserService;
import com.lab.reagent.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 登录Controller
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    
    @Autowired
    private SysUserService sysUserService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginVO loginVO = sysUserService.login(loginDTO);
            return Result.success(loginVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}








package com.lab.reagent.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.reagent.dto.LoginDTO;
import com.lab.reagent.entity.SysUser;
import com.lab.reagent.mapper.SysUserMapper;
import com.lab.reagent.util.JwtUtil;
import com.lab.reagent.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户Service
 */
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * 性能优化版本
     */
    public LoginVO login(LoginDTO loginDTO) {
        // 优化1: 直接使用Mapper查询，减少对象包装
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername())
                .select(SysUser::getId, SysUser::getUsername, SysUser::getPassword,
                        SysUser::getRealName, SysUser::getRole, SysUser::getDepartment,
                        SysUser::getStatus); // 只选择需要的字段
        SysUser user = this.baseMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误"); // 安全优化：不透露具体错误
        }

        // 优化2: 提前检查状态，避免不必要的加密计算
        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用");
        }

        // 优化3: 密码验证（MD5加密）
        String encryptedPassword = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new RuntimeException("用户名或密码错误"); // 安全优化：不透露具体错误
        }

        // 优化4: 直接构建返回对象，减少中间步骤
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .department(user.getDepartment())
                .build();
    }

    /**
     * 添加用户
     */
    public void addUser(SysUser user) {
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查是否添加管理员角色 - 系统中只能有一个管理员
        if ("ADMIN".equals(user.getRole())) {
            LambdaQueryWrapper<SysUser> adminWrapper = new LambdaQueryWrapper<>();
            adminWrapper.eq(SysUser::getRole, "ADMIN");
            if (this.count(adminWrapper) > 0) {
                throw new RuntimeException("系统中只能有一个管理员账户");
            }
        }

        // 密码MD5加密
        user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        user.setStatus(1);

        this.save(user);
    }

    /**
     * 更新用户
     */
    public void updateUser(SysUser user) {
        SysUser existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果原来是管理员，不允许修改角色
        if ("ADMIN".equals(existUser.getRole()) && !("ADMIN".equals(user.getRole()))) {
            throw new RuntimeException("不允许修改管理员的角色");
        }

        // 如果修改为管理员角色，检查系统中是否已有管理员
        if ("ADMIN".equals(user.getRole()) && !("ADMIN".equals(existUser.getRole()))) {
            LambdaQueryWrapper<SysUser> adminWrapper = new LambdaQueryWrapper<>();
            adminWrapper.eq(SysUser::getRole, "ADMIN");
            if (this.count(adminWrapper) > 0) {
                throw new RuntimeException("系统中只能有一个管理员账户");
            }
        }

        this.updateById(user);
    }

    /**
     * 更新用户状态
     */
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 禁止禁用管理员账户
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不允许禁用系统管理员账户");
        }

        user.setStatus(status);
        this.updateById(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 禁止删除管理员账户
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不允许删除系统管理员账户");
        }

        this.removeById(userId);
    }
}

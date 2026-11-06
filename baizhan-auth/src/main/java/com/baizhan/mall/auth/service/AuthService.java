package com.baizhan.mall.auth.service;

import com.baizhan.mall.auth.dto.LoginDTO;
import com.baizhan.mall.auth.dto.RegisterDTO;
import com.baizhan.mall.auth.entity.Admin;
import com.baizhan.mall.auth.entity.ShoppingUser;
import com.baizhan.mall.auth.mapper.AdminMapper;
import com.baizhan.mall.auth.mapper.ShoppingUserMapper;
import com.baizhan.mall.auth.vo.LoginVO;
import com.baizhan.mall.common.core.exception.BizException;
import com.baizhan.mall.common.core.result.ResultCode;
import com.baizhan.mall.common.security.JwtProperties;
import com.baizhan.mall.common.security.JwtUtil;
import com.baizhan.mall.common.security.PasswordUtil;
import com.baizhan.mall.common.security.TokenUser;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final AdminMapper adminMapper;
    private final ShoppingUserMapper shoppingUserMapper;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public AuthService(AdminMapper adminMapper, ShoppingUserMapper shoppingUserMapper,
                       JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.adminMapper = adminMapper;
        this.shoppingUserMapper = shoppingUserMapper;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    /** 登录：admin 查 bz_admin，user 查 bz_shopping_user */
    public LoginVO login(LoginDTO dto) {
        Long userId;
        String username;
        String encodedPassword;

        if (TokenUser.TYPE_ADMIN.equalsIgnoreCase(dto.getType())) {
            Admin admin = adminMapper.selectOne(Wrappers.<Admin>lambdaQuery()
                    .eq(Admin::getUsername, dto.getUsername())
                    .last("limit 1"));
            if (admin == null) {
                throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
            }
            userId = admin.getAid();
            username = admin.getUsername();
            encodedPassword = admin.getPassword();
        } else {
            ShoppingUser user = shoppingUserMapper.selectOne(Wrappers.<ShoppingUser>lambdaQuery()
                    .eq(ShoppingUser::getUsername, dto.getUsername())
                    .last("limit 1"));
            if (user == null) {
                throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
            }
            if ("N".equalsIgnoreCase(user.getStatus())) {
                throw new BizException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
            }
            userId = user.getId();
            username = user.getUsername();
            encodedPassword = user.getPassword();
        }

        if (!PasswordUtil.matches(dto.getPassword(), encodedPassword)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        String type = TokenUser.TYPE_ADMIN.equalsIgnoreCase(dto.getType())
                ? TokenUser.TYPE_ADMIN : TokenUser.TYPE_USER;
        String token = jwtUtil.generate(userId, username, type);
        return new LoginVO(token, jwtProperties.getPrefix().trim(),
                jwtProperties.getExpiration(), userId, username, type);
    }

    /** C 端注册（BCrypt 存储） */
    public void register(RegisterDTO dto) {
        Long count = shoppingUserMapper.selectCount(Wrappers.<ShoppingUser>lambdaQuery()
                .eq(ShoppingUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BizException("用户名已存在");
        }
        ShoppingUser user = new ShoppingUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setStatus("Y");
        shoppingUserMapper.insert(user);
        log.info("[注册成功] username={}", dto.getUsername());
    }
}

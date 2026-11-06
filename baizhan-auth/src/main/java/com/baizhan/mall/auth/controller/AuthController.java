package com.baizhan.mall.auth.controller;

import com.baizhan.mall.auth.dto.LoginDTO;
import com.baizhan.mall.auth.dto.RegisterDTO;
import com.baizhan.mall.auth.service.AuthService;
import com.baizhan.mall.auth.vo.LoginVO;
import com.baizhan.mall.common.core.result.Result;
import com.baizhan.mall.common.security.JwtProperties;
import com.baizhan.mall.common.security.JwtUtil;
import com.baizhan.mall.common.security.TokenUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证中心")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @Operation(summary = "C端注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @Operation(summary = "令牌校验（返回当前登录用户）")
    @GetMapping("/check")
    public Result<TokenUser> check(@RequestHeader(name = "Authorization") String authorization) {
        String token = authorization.substring(jwtProperties.getPrefix().length()).trim();
        TokenUser tokenUser = jwtUtil.parse(token);
        if (tokenUser == null) {
            return Result.fail(401, "令牌无效或已过期");
        }
        return Result.success(tokenUser);
    }
}

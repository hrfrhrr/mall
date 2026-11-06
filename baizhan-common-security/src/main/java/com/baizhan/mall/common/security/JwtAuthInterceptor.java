package com.baizhan.mall.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务级 JWT 鉴权拦截器：
 * 1. 命中白名单直接放行；
 * 2. 校验 Authorization: Bearer <token>，通过后注入 UserContext；
 * 3. 失败返回 401 JSON。
 */
@Slf4j
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final JwtAuthProperties authProperties;

    public JwtAuthInterceptor(JwtUtil jwtUtil, JwtProperties jwtProperties, JwtAuthProperties authProperties) {
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.authProperties = authProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (isSkip(path)) {
            return true;
        }
        String header = request.getHeader(jwtProperties.getHeader());
        String prefix = jwtProperties.getPrefix();
        if (header == null || !header.startsWith(prefix)) {
            writeUnauthorized(response, "未登录或令牌缺失");
            return false;
        }
        TokenUser user = jwtUtil.parse(header.substring(prefix.length()).trim());
        if (user == null) {
            writeUnauthorized(response, "令牌无效或已过期");
            return false;
        }
        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isSkip(String path) {
        return authProperties.getSkipPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>(4);
        body.put("code", 401);
        body.put("message", message);
        body.put("data", null);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(body));
    }
}

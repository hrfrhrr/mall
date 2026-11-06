package com.baizhan.mall.common.security;

/**
 * 当前登录用户上下文（ThreadLocal，请求结束必须清理）。
 * 由 JwtAuthInterceptor 在鉴权通过后注入。
 */
public final class UserContext {

    private static final ThreadLocal<TokenUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(TokenUser user) {
        HOLDER.set(user);
    }

    /** 获取当前用户，未登录返回 null */
    public static TokenUser get() {
        return HOLDER.get();
    }

    /** 获取当前用户ID，未登录返回 null */
    public static Long getUserId() {
        TokenUser user = HOLDER.get();
        return user == null ? null : user.userId();
    }

    public static String getUsername() {
        TokenUser user = HOLDER.get();
        return user == null ? null : user.username();
    }

    public static String getUserType() {
        TokenUser user = HOLDER.get();
        return user == null ? null : user.type();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

package com.baizhan.mall.common.security;

public record TokenUser(Long userId, String username, String type) {

    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_USER = "user";
}

package com.baizhan.mall.common.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 密码工具。
 * <p>
 * 兼容三种存储格式，方便旧库平滑迁移：
 * 1. BCrypt（推荐，新注册用户一律使用）；
 * 2. 32 位 MD5（历史遗留数据，仅校验，登录后应引导升级）；
 * 3. 明文（仅限演示脏数据，仅校验）。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** BCrypt 加密（新密码请用此方法） */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")
                || encodedPassword.startsWith("$2y$")) {
            return ENCODER.matches(rawPassword, encodedPassword);
        }
        if (encodedPassword.matches("[0-9a-fA-F]{32}")) {
            String md5 = DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
            return md5.equalsIgnoreCase(encodedPassword);
        }
        return encodedPassword.equals(rawPassword);
    }
}

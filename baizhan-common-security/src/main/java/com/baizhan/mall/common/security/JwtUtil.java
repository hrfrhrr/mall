package com.baizhan.mall.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_USER_TYPE = "type";

    @Autowired
    private JwtProperties properties;

    private volatile SecretKey secretKey;

    private SecretKey key() {
        if (secretKey == null) {
            synchronized (this) {
                if (secretKey == null) {
                    byte[] bytes;
                    try {
                        bytes = Base64.getDecoder().decode(properties.getSecret());
                    } catch (IllegalArgumentException e) {
                        bytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
                    }
                    secretKey = Keys.hmacShaKeyFor(bytes);
                }
            }
        }
        return secretKey;
    }

    /** 签发令牌 */
    public String generate(Long userId, String username, String type) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USER_TYPE, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getExpiration() * 1000))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    /** 解析令牌，失败（过期/篡改）返回 null */
    public TokenUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object uid = claims.get(CLAIM_USER_ID);
            return new TokenUser(
                    uid == null ? null : Long.valueOf(String.valueOf(uid)),
                    claims.getSubject(),
                    claims.get(CLAIM_USER_TYPE, String.class));
        } catch (Exception e) {
            log.debug("[JWT] 解析失败: {}", e.getMessage());
            return null;
        }
    }
}

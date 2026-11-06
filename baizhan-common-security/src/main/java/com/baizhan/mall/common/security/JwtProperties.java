package com.baizhan.mall.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置。密钥为 Base64 编码（HS256 要求 >= 256bit），生产环境务必通过环境变量覆盖。
 */
@Data
@ConfigurationProperties(prefix = "baizhan.jwt")
public class JwtProperties {

    /** Base64 密钥 */
    private String secret = "YmFpemhhbi1tYWxsLWp3dC1zaWduLXNlY3JldC0yMDI0LTAxLTAx";

    /** 过期时间（秒），默认 2 小时 */
    private long expiration = 7200;

    /** 请求头 */
    private String header = "Authorization";

    /** 令牌前缀 */
    private String prefix = "Bearer ";
}

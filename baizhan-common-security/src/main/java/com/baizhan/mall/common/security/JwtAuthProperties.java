package com.baizhan.mall.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务级 JWT 鉴权配置。
 * 网关（Higress）只做路由后，各业务服务自行校验令牌，
 * 无状态共享密钥，与网关鉴权等价。
 */
@Data
@ConfigurationProperties(prefix = "baizhan.jwt-auth")
public class JwtAuthProperties {

    /** 是否启用服务级鉴权，默认开启 */
    private boolean enabled = true;

    /** 鉴权白名单（Ant 风格），命中则直接放行 */
    private List<String> skipPaths = new ArrayList<>();
}

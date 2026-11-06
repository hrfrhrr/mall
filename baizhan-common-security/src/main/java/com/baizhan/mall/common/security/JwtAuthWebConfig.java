package com.baizhan.mall.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册服务级 JWT 鉴权拦截器（仅 Servlet Web 应用生效）。
 * JwtAuthProperties 由各服务的 @ConfigurationPropertiesScan 注册。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JwtAuthWebConfig {

    @Bean
    public WebMvcConfigurer jwtAuthWebMvcConfigurer(JwtUtil jwtUtil, JwtProperties jwtProperties,
                                                    JwtAuthProperties authProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                if (authProperties.isEnabled()) {
                    registry.addInterceptor(new JwtAuthInterceptor(jwtUtil, jwtProperties, authProperties))
                            .addPathPatterns("/**");
                }
            }
        };
    }
}

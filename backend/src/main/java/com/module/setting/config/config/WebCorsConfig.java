package com.module.setting.config.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 表現層跨來源資源共用配置類 (WebCorsConfig)
 * <p>
 * 集中於專案根目錄下的 config 套件，負責配置跨來源請求 (CORS) 策略，
 * 支援前端獨立專案 (如 Angular、React 等) 進行跨域調用與 X-Tenant-Id 自訂標頭傳遞。
 * </p>
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("X-Tenant-Id", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

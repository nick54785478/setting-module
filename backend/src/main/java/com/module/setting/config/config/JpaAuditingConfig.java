package com.module.setting.config.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 審計配置類 (JpaAuditingConfig)
 * <p>
 * 集中於專案根目錄下的 config 套件，啟用 Spring Data JPA 的 Auditing 機制，
 * 支援實體監聽器 (AuditingEntityListener) 的自動審計。
 * </p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

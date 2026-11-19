-- =============================================================================
-- Setting Module 資料庫結構定義 (DDL - Schema Definition)
-- 適用資料庫: MySQL 8.0+
-- 字符集: utf8mb4 / 校對規則: utf8mb4_unicode_ci
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. 表格: setting_category (維度元資料字典表)
-- 說明: 獨立管理系統業務大分類字典 (Meta Category)，避免業務設定與維度字典混淆
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `setting_category`;
CREATE TABLE `setting_category` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT                COMMENT '主鍵 ID (自動遞增)',
    `tenant`        VARCHAR(64)     NOT NULL DEFAULT 'CW'                  COMMENT '租戶識別代碼 (多租戶隔離)',
    `code`          VARCHAR(64)     NOT NULL                               COMMENT '維度唯一代碼 (例如: SYSTEM, LOCALE, MENU, DYNAMIC_PAGE)',
    `name`          VARCHAR(128)    NOT NULL                               COMMENT '維度中文名稱 (例如: 系統配置, 語系配置)',
    `description`   VARCHAR(512)    NULL                                   COMMENT '維度詳細功能說明',
    `priority_no`   INT             NOT NULL DEFAULT 1                     COMMENT '顯示排序序號 (升冪排序)',
    `active_flag`   VARCHAR(1)      NOT NULL DEFAULT 'Y'                   COMMENT '是否有效旗標 (Y: 有效, N: 停用)',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '建立時間戳記',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間戳記',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_setting_category_tenant_code` (`tenant`, `code`),
    KEY `idx_category_tenant_active_priority` (`tenant`, `active_flag`, `priority_no`),
    KEY `idx_category_priority` (`priority_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='維度元資料字典表';

-- -----------------------------------------------------------------------------
-- 2. 表格: setting (核心業務功能設定與多租戶動態載體表)
-- 說明: 遵循 Function Configurable 四維坐標定位 (tenant, category, sub_category, code)
--       支援儲存系統功能開關、業務參數、國際化語系、側邊選單以及大容量動態表單 JSON (TEXT)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `setting`;
CREATE TABLE `setting` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT                COMMENT '主鍵 ID (自動遞增)',
    `tenant`        VARCHAR(64)     NOT NULL DEFAULT 'CW'                  COMMENT '租戶識別代碼 (多租戶空間隔離)',
    `category`      VARCHAR(64)     NOT NULL                               COMMENT '功能維度大類 (例如: SYSTEM, LOCALE, MENU, DYNAMIC_PAGE)',
    `sub_category`  VARCHAR(64)     NULL                                   COMMENT '業務子模組/二級分組 (例如: UI, 核心業務配置, PAGE_SCHEMA)',
    `code`          VARCHAR(64)     NOT NULL                               COMMENT '設定項目唯一業務代碼 (例如: zh-TW, MENU_TENANT, TENANT_PROFILE_SCHEMA)',
    `name`          VARCHAR(128)    NULL                                   COMMENT '功能設定顯示名稱 (例如: 繁體中文, 租戶畫像資料)',
    `description`   VARCHAR(512)    NULL                                   COMMENT '功能設定詳細用途說明',
    `value`         TEXT            NULL                                   COMMENT '設定內容/路由標識/動態 JSON 結構定義或實例資料',
    `priority_no`   INT             NOT NULL DEFAULT 1                     COMMENT '順序號 (決定清單或選單排序)',
    `active_flag`   VARCHAR(1)      NOT NULL DEFAULT 'Y'                   COMMENT '是否有效旗標 (Y: 有效, N: 停用)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_setting_scope` (`tenant`, `category`, `code`),
    KEY `idx_tenant_category_priority` (`tenant`, `category`, `priority_no`),
    KEY `idx_tenant_active_priority` (`tenant`, `active_flag`, `priority_no`),
    KEY `idx_priority_no` (`priority_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='功能設定與動態資料載體表';

-- -----------------------------------------------------------------------------
-- 3. 表格: customisation (使用者個人化偏好與表格欄位自訂顯示表)
-- 說明: 支援各租戶各使用者針對前端畫面 (如 SettingComponent) 自訂欄位顯示 (FieldView)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `customisation`;
CREATE TABLE `customisation` (
    `id`                 BIGINT          NOT NULL AUTO_INCREMENT           COMMENT '主鍵 ID (自動遞增)',
    `tenant`             VARCHAR(64)     NOT NULL DEFAULT 'CW'             COMMENT '租戶代碼 (多租戶隔離)',
    `username`           VARCHAR(128)    NOT NULL                          COMMENT '使用者帳號 (例如: nickgh.zhang@cw.com)',
    `component`          VARCHAR(64)     NOT NULL                          COMMENT '前端元件識別碼 (例如: SettingComponent)',
    `type`               VARCHAR(64)     NOT NULL                          COMMENT '偏好類型 (例如: FieldView)',
    `value`              TEXT            NOT NULL                          COMMENT '偏好內容值 (例如 CSV 欄位清單)',
    `active_flag`        VARCHAR(1)      NOT NULL DEFAULT 'Y'              COMMENT '有效性旗標 (Y: 有效, N: 停用)',
    `created_date`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間戳記',
    `last_modified_date` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後異動時間戳記',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_customisation_scope` (`tenant`, `username`, `component`, `type`),
    KEY `idx_customisation_lookup` (`tenant`, `username`, `component`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者個人化配置偏好表';

SET FOREIGN_KEY_CHECKS = 1;

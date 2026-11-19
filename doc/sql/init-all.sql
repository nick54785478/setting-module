-- =============================================================================
-- Setting Module 完整資料庫建表與初始化種子腳本 (DDL + DML)
-- 適用資料庫: MySQL 8.0+
-- 字符集: utf8mb4 / 校對規則: utf8mb4_unicode_ci
-- 預設租戶: CW
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 第一部分：結構定義 (DDL - Data Definition Language)
-- =============================================================================

-- 1. 表格: setting_category (維度元資料字典表)
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

-- 2. 表格: setting (核心業務功能設定與多租戶動態載體表)
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

-- 3. 表格: customisation (使用者個人化偏好與表格欄位自訂顯示表)
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

-- =============================================================================
-- 第二部分：種子資料初始化 (DML - Data Manipulation Language)
-- =============================================================================

-- 1. 初始化維度元資料字典 (setting_category)
INSERT INTO `setting_category` (`tenant`, `code`, `name`, `description`, `priority_no`, `active_flag`)
VALUES
('CW', 'SYSTEM', '系統配置', '系統底層與全域基礎設施配置維度', 1, 'Y'),
('CW', 'LOCALE', '系統語系配置', '管理各租戶支援的系統語言環境與國際化設定', 2, 'Y'),
('CW', 'MENU', '側邊欄選單配置', '管理側邊導航列之分組、項目、圖示、路由與顯示順序', 3, 'Y'),
('CW', 'DYNAMIC_PAGE', '動態頁面配置', '管理各租戶專屬自訂動態頁面與表單填寫資料', 4, 'Y');

-- 2. 初始化基準國際化語系 (setting - LOCALE)
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
VALUES
('CW', 'LOCALE', 'UI', 'zh-TW', '繁體中文', '繁體中文 (台灣)', 'DEFAULT', 1, 'Y'),
('CW', 'LOCALE', 'UI', 'en-US', 'English', 'English (United States)', '', 2, 'Y'),
('CW', 'LOCALE', 'UI', 'ja-JP', '日本語', '日本語 (日本)', '', 3, 'Y');

-- 3. 初始化側邊欄導航選單 (setting - MENU)
-- 3.1 核心業務配置群組
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
VALUES
('CW', 'MENU', '核心業務配置', 'MENU_SETTING', '功能設定管理', '核心業務設定項目增刪改查管理', '{"icon":"pi pi-sliders-h","badge":"核心","view":"setting"}', 1, 'Y'),
('CW', 'MENU', '核心業務配置', 'MENU_CATEGORY', '元資料字典', '維度大分類字典維護', '{"icon":"pi pi-database","badge":"元資料","view":"category"}', 2, 'Y'),
('CW', 'MENU', '核心業務配置', 'MENU_DESIGNER', '動態表單設計', '視覺化自訂租戶動態表單欄位與配置', '{"icon":"pi pi-pencil","badge":"設計","view":"dynamic-designer"}', 3, 'Y'),
('CW', 'MENU', '核心業務配置', 'MENU_TENANT', '租戶畫像資料', '多租戶專屬自訂畫像資料與動態表單配置', '{"icon":"pi pi-building","view":"tenant-profile"}', 4, 'Y'),

-- 3.2 系統與安全維運群組
('CW', 'MENU', '系統與安全維運', 'MENU_ROLE', '系統權限角色', 'RBAC 角色與細粒度操作授權矩陣', '{"icon":"pi pi-shield"}', 1, 'Y'),
('CW', 'MENU', '系統與安全維運', 'MENU_AUDIT', '操作稽核日誌', '使用者操作變更歷史與安全稽核軌跡', '{"icon":"pi pi-file-edit"}', 2, 'Y'),
('CW', 'MENU', '系統與安全維運', 'MENU_API_DOCS', 'API 規格文件', 'OpenAPI 3.0 與 Swagger 互動式 API 文件', '{"icon":"pi pi-book","url":"/swagger-ui.html"}', 3, 'Y');

-- 4. 初始化動態表單結構與實例資料 (setting - DYNAMIC_PAGE)
-- 4.1 租戶畫像表單結構 (TENANT_PROFILE_SCHEMA)
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
VALUES
('CW', 'DYNAMIC_PAGE', 'PAGE_SCHEMA', 'TENANT_PROFILE_SCHEMA', 'TENANT_PROFILE Schema', 'Dynamic Page Form Schema for TENANT_PROFILE',
'[
  {
    "name": "tenantName",
    "type": "text",
    "label": "租戶正式名稱",
    "validators": ["required"],
    "placeholder": "請輸入租戶企業名稱"
  },
  {
    "name": "tenantCode",
    "type": "text",
    "label": "租戶識別代碼",
    "validators": ["required"],
    "placeholder": "例如: CW, ACME"
  },
  {
    "name": "contactEmail",
    "type": "email",
    "label": "系統管理員聯絡信箱",
    "validators": ["required", "email"],
    "placeholder": "admin@example.com"
  },
  {
    "name": "maxUsers",
    "type": "number",
    "label": "授權使用者上限",
    "validators": ["required"],
    "placeholder": "例如: 100"
  },
  {
    "name": "tier",
    "type": "dropdown",
    "label": "租戶合約等級方案",
    "validators": ["required"],
    "options": [
      { "label": "標準版 (Standard)", "value": "STANDARD" },
      { "label": "企業版 (Enterprise)", "value": "ENTERPRISE" },
      { "label": "旗艦版 (Ultimate)", "value": "ULTIMATE" }
    ],
    "placeholder": "請選擇等級方案"
  },
  {
    "name": "contractExpiry",
    "type": "calendar",
    "label": "授權合約到期日",
    "validators": ["required"],
    "pattern": "yy-mm-dd",
    "placeholder": "yy-mm-dd"
  },
  {
    "name": "dataRegion",
    "type": "autocomplete",
    "label": "數據隔離託管區域",
    "validators": [],
    "placeholder": "搜尋或輸入資料中心區域 (如: 台北, 東京, 美西)",
    "uri": "regions"
  }
]', 1, 'Y');

-- 4.2 租戶畫像填寫實例資料 (TENANT_PROFILE)
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
VALUES
('CW', 'DYNAMIC_PAGE', 'PAGE_DATA', 'TENANT_PROFILE', 'TENANT_PROFILE', 'Dynamic Page Form Data for TENANT_PROFILE',
'{"tenantCode":"CW","tenantName":"Chrono Weaver","contactEmail":"admin@cw.com","maxUsers":500,"tier":"ENTERPRISE","contractExpiry":"2027-12-31","dataRegion":"TW-TPE-1"}', 1, 'Y');

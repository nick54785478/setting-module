-- =============================================================================
-- Setting Module 初始化種子資料 (DML - Seed Data)
-- 適用租戶: CW (可直接將 'CW' 全域取代為您的目標 Tenant 代碼)
-- =============================================================================

SET NAMES utf8mb4;

-- =============================================================================
-- 1. 初始化維度元資料字典 (setting_category)
-- =============================================================================
INSERT INTO `setting_category` (`tenant`, `code`, `name`, `description`, `priority_no`, `active_flag`)
VALUES
('CW', 'SYSTEM', '系統配置', '系統底層與全域基礎設施配置維度', 1, 'Y'),
('CW', 'LOCALE', '系統語系配置', '管理各租戶支援的系統語言環境與國際化設定', 2, 'Y'),
('CW', 'MENU', '側邊欄選單配置', '管理側邊導航列之分組、項目、圖示、路由與顯示順序', 3, 'Y'),
('CW', 'DYNAMIC_PAGE', '動態頁面配置', '管理各租戶專屬自訂動態頁面與表單填寫資料', 4, 'Y')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `priority_no` = VALUES(`priority_no`),
    `active_flag` = VALUES(`active_flag`);

-- =============================================================================
-- 2. 初始化基準國際化語系 (setting - LOCALE)
-- =============================================================================
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
VALUES
('CW', 'LOCALE', 'UI', 'zh-TW', '繁體中文', '繁體中文 (台灣)', 'DEFAULT', 1, 'Y'),
('CW', 'LOCALE', 'UI', 'en-US', 'English', 'English (United States)', '', 2, 'Y'),
('CW', 'LOCALE', 'UI', 'ja-JP', '日本語', '日本語 (日本)', '', 3, 'Y')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `value` = VALUES(`value`),
    `priority_no` = VALUES(`priority_no`),
    `active_flag` = VALUES(`active_flag`);

-- =============================================================================
-- 3. 初始化側邊欄導航選單 (setting - MENU)
-- =============================================================================
-- 分組一：核心業務配置
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
VALUES
('CW', 'MENU', '核心業務配置', 'MENU_SETTING', '功能設定管理', '核心業務設定項目增刪改查管理', '{"icon":"pi pi-sliders-h","badge":"核心","view":"setting"}', 1, 'Y'),
('CW', 'MENU', '核心業務配置', 'MENU_CATEGORY', '元資料字典', '維度大分類字典維護', '{"icon":"pi pi-database","badge":"元資料","view":"category"}', 2, 'Y'),
('CW', 'MENU', '核心業務配置', 'MENU_LOCALE', '系統語系設定', '多租戶支援之國際化語系維護與預設值配置', '{"icon":"pi pi-globe","badge":"語系","view":"locale"}', 3, 'Y'),
('CW', 'MENU', '核心業務配置', 'MENU_DESIGNER', '動態表單設計', '視覺化自訂租戶動態表單欄位與配置', '{"icon":"pi pi-pencil","badge":"設計","view":"dynamic-designer"}', 4, 'Y'),
('CW', 'MENU', '核心業務配置', 'MENU_TENANT', '租戶畫像資料', '多租戶專屬自訂畫像資料與動態表單配置', '{"icon":"pi pi-building","view":"tenant-profile"}', 5, 'Y'),

-- 分組二：系統與安全維運
('CW', 'MENU', '系統與安全維運', 'MENU_ROLE', '系統權限角色', 'RBAC 角色與細粒度操作授權矩陣', '{"icon":"pi pi-shield"}', 1, 'Y'),
('CW', 'MENU', '系統與安全維運', 'MENU_AUDIT', '操作稽核日誌', '使用者操作變更歷史與安全稽核軌跡', '{"icon":"pi pi-file-edit"}', 2, 'Y'),
('CW', 'MENU', '系統與安全維運', 'MENU_API_DOCS', 'API 規格文件', 'OpenAPI 3.0 與 Swagger 互動式 API 文件', '{"icon":"pi pi-book","url":"/swagger-ui.html"}', 3, 'Y')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `value` = VALUES(`value`),
    `priority_no` = VALUES(`priority_no`),
    `active_flag` = VALUES(`active_flag`);

-- =============================================================================
-- 4. 初始化動態表單結構與實例資料 (setting - DYNAMIC_PAGE)
-- =============================================================================
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
]', 1, 'Y')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `value` = VALUES(`value`),
    `priority_no` = VALUES(`priority_no`),
    `active_flag` = VALUES(`active_flag`);

-- 4.2 租戶畫像填寫實例資料 (TENANT_PROFILE)
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
VALUES
('CW', 'DYNAMIC_PAGE', 'PAGE_DATA', 'TENANT_PROFILE', 'TENANT_PROFILE', 'Dynamic Page Form Data for TENANT_PROFILE',
'{"tenantCode":"CW","tenantName":"Chrono Weaver","contactEmail":"admin@cw.com","maxUsers":500,"tier":"ENTERPRISE","contractExpiry":"2027-12-31","dataRegion":"TW-TPE-1"}', 1, 'Y')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `value` = VALUES(`value`),
    `priority_no` = VALUES(`priority_no`),
    `active_flag` = VALUES(`active_flag`);

# 系統設定與維度字典手動配置作業手冊 (Manual Configuration Guide)
## 模組名稱：Setting Module - 核心業務維度元資料與業務設定手動配置全景指南

---

### 1. 文件資訊 (Document Metadata)
* **文件版本**：v2.0.0 (Unified & Decoupled)
* **發布日期**：2026-11-16
* **適用模組**：`setting-module` (Backend: Spring Boot 4 / Frontend: Angular 18 + PrimeNG 18)
* **適用對象**：維運工程師 (SRE / DevOps)、系統管理員 (Admin)、資料庫管理員 (DBA)、開發人員
* **整併說明**：本手冊完整整合並取代原分立之 `docs/MANUAL_CONFIGURATION_GUIDE.md` 與舊版 `doc/guide/manual-setting-configuration-guide.md`，全面基於「獨立維度字典實體 (`SettingCategory`) 與業務設定 (`Setting`) 解耦架構」撰寫。

---

### 2. 架構解耦與純淨基線原則 (Architecture & Principles)

#### 2.1 架構解耦說明
在舊版設計中，大分類維度（如 `SYSTEM`, `ROLE`, `GROUP`, `FUNCTION`）以 `category = 'META'` 假資料的形式混雜於 `setting` 業務設定表中，導致下拉選單混亂、二級聯動邏輯過度耦合。
在全新的 **v2.0.0** 架構中：
* **`setting_category` 表**：專門儲存全系統大維度元資料字典（Meta Category）。
* **`setting` 表**：專門儲存具體業務設定項目，其 `scope.category` 關聯至 `setting_category.code`，不再包含任何 `META` 假資料。
* **介面徹底分離**：
  - **元資料字典維護**：負責大維度之新增、編輯與啟用/停用。
  - **功能設定管理**：負責具體業務設定之維護，其下拉選單直接來自 `setting_category`。

#### 2.2 初始內建基線 (Zero-Pollution Baseline)
系統首次部署啟動時，為了維持資料庫純淨：
* **`setting_category` 表**：僅預載 1 筆全域基礎設施維度 `SYSTEM`（系統配置）。
* **`setting` 表**：初始筆數嚴格為 **0 筆**（純淨無假資料）。
* **其餘核心維度 (`ROLE`, `GROUP`, `FUNCTION`) 與具體業務參數，全數依循本手冊手動建立**。

---

### 3. 標準待配置資料矩陣 (Standard Specification Matrix)

以下為需配置回系統的三大標準業務大維度標準值：

| 欄位名稱 (Field) | 維度 1：角色配置 (ROLE) | 維度 2：群組配置 (GROUP) | 維度 3：功能配置 (FUNCTION) | 說明與約束 |
| :--- | :--- | :--- | :--- | :--- |
| **目標資料表** | `setting_category` | `setting_category` | `setting_category` | 獨立維度字典表 |
| **所屬租戶 (Tenant)** | `CW`（依租戶各自獨立） | `CW`（依租戶各自獨立） | `CW`（依租戶各自獨立） | 已擱置全域 ALL，各租戶獨立定義 |
| **維度代碼 (Code)** | `ROLE` | `GROUP` | `FUNCTION` | 唯一代碼 (Unique Key) |
| **維度名稱 (Name)** | `角色配置` | `群組配置` | `功能配置` | 中文顯示標籤 (前端 Label) |
| **詳細說明 (Description)**| `業務角色權限分類維度` | `業務組織群組分類維度` | `前端功能與頁面模組配置維度`| 維度業務範疇說明 |
| **排序序號 (Priority No)**| `2` | `3` | `4` | 控制選單排序 (升冪) |
| **有效狀態 (ActiveFlag)**| `Y` | `Y` | `Y` | `Y`: 有效, `N`: 停用 |

---

### 4. 實作方式一：透過前端 UI 介面配置 (GUI Console)

適合人員：**後台系統管理員、業務維運人員**

#### 階段一：建立「元資料字典 (Category)」
1. 進入系統控制台前端頁面：`http://localhost:4200/`。
2. 點擊左側導航選單「**核心業務配置**」>「**元資料字典**」。
3. 點選右上角「**+ 新增維度字典**」按鈕，依序建立 3 筆維度：
   * **第 1 筆 (ROLE)**：
     - 所屬租戶：`CW`（鎖定預設）
     - 排序序號：`2`
     - 維度唯一代碼：`ROLE`
     - 維度顯示名稱：`角色配置`
     - 詳細說明：`業務角色權限分類維度`
   * **第 2 筆 (GROUP)**：
     - 所屬租戶：`CW`
     - 排序序號：`3`
     - 維度唯一代碼：`GROUP`
     - 維度顯示名稱：`群組配置`
     - 詳細說明：`業務組織群組分類維度`
   * **第 3 筆 (FUNCTION)**：
     - 所屬租戶：`CW`
     - 排序序號：`4`
     - 維度唯一代碼：`FUNCTION`
     - 維度顯示名稱：`功能配置`
     - 詳細說明：`前端功能與頁面模組配置維度`
4. 點擊「**確認儲存**」，表格即時呈現剛建立的 3 筆維度。

#### 階段二：建立具體「功能業務設定 (Setting)」
維度建立完成後，即可至設定頁面建立各模組參數：
1. 點擊左側選單「**核心業務配置**」>「**功能設定管理**」。
2. 點擊右上角「**+ 新增設定項目**」按鈕。
3. 填寫欄位資訊：
   - **大分類 (Category)**：下拉選擇剛建立的維度（例如 `角色配置 (ROLE)` 或 `功能配置 (FUNCTION)`）。
   - **子模組 / 類型 (SubCategory)**：自訂模組分類代碼（例如 `SESSION`、`SECURITY`、`DASHBOARD`）。
   - **設定代碼 (Code)**：唯一業務代碼（例如 `MAX_ACTIVE_SESSIONS`）。
   - **顯示名稱 (Name)**：例如 `最大同時登入工作階段數`。
   - **設定數值 (Value)**：具體配置值（例如 `3`）。
   - **排序序號 (Priority No)**：例如 `1`。
4. 點擊「**確認儲存**」。

---

### 5. 實作方式二：透過 RESTful API 配置 (cURL / CI/CD 腳本)

適合人員：**DevOps、自動化維運腳本、API 測試人員**

#### 階段一：透過維度 API 建立大分類 (`POST /api/v1/categories`)

```bash
# 1. 建立 ROLE 角色配置維度
curl -X POST "http://localhost:8080/api/v1/categories" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: CW" \
  -d '{
    "tenant": "CW",
    "code": "ROLE",
    "name": "角色配置",
    "description": "業務角色權限分類維度",
    "priorityNo": 2
  }'

# 2. 建立 GROUP 群組配置維度
curl -X POST "http://localhost:8080/api/v1/categories" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: CW" \
  -d '{
    "tenant": "CW",
    "code": "GROUP",
    "name": "群組配置",
    "description": "業務組織群組分類維度",
    "priorityNo": 3
  }'

# 3. 建立 FUNCTION 功能配置維度
curl -X POST "http://localhost:8080/api/v1/categories" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: CW" \
  -d '{
    "tenant": "CW",
    "code": "FUNCTION",
    "name": "功能配置",
    "description": "前端功能與頁面模組配置維度",
    "priorityNo": 4
  }'
```

#### 階段二：透過設定 API 建立具體業務參數 (`POST /api/v1/settings`)

```bash
# 建立 ROLE 維度下的 SESSION 逾時設定
curl -X POST "http://localhost:8080/api/v1/settings" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: CW" \
  -d '{
    "tenant": "CW",
    "category": "ROLE",
    "subCategory": "SESSION",
    "code": "SESSION_TIMEOUT_MINUTES",
    "name": "連線逾時分鐘數",
    "value": "30",
    "description": "使用者無操作時之登出時間設定",
    "priorityNo": 1
  }'

# 建立 FUNCTION 維度下的儀表板分析功能開關
curl -X POST "http://localhost:8080/api/v1/settings" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: CW" \
  -d '{
    "tenant": "CW",
    "category": "FUNCTION",
    "subCategory": "DASHBOARD",
    "code": "DASHBOARD_ANALYTICS",
    "name": "儀表板數據分析模組",
    "value": "ENABLED",
    "description": "控制首頁即時分析圖表呈現",
    "priorityNo": 1
  }'
```

---

### 6. 實作方式三：透過 SQL 直接寫入資料庫 (Direct Database Script)

適合人員：**DBA、資料庫遷移自動化作業**

直接連線至 MySQL (`settingdb`)，執行以下具備**冪等性防護**之 SQL 腳本：

```sql
USE `settingdb`;

-- ========================================================
-- 階段一：寫入 setting_category 維度字典表
-- ========================================================

-- 1. 寫入 ROLE 角色配置維度
INSERT INTO `setting_category` (`tenant`, `code`, `name`, `description`, `priority_no`, `active_flag`, `created_at`, `updated_at`)
SELECT 'CW', 'ROLE', '角色配置', '業務角色權限分類維度', 2, 'Y', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `setting_category` WHERE `tenant` = 'CW' AND `code` = 'ROLE'
);

-- 2. 寫入 GROUP 群組配置維度
INSERT INTO `setting_category` (`tenant`, `code`, `name`, `description`, `priority_no`, `active_flag`, `created_at`, `updated_at`)
SELECT 'CW', 'GROUP', '群組配置', '業務組織群組分類維度', 3, 'Y', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `setting_category` WHERE `tenant` = 'CW' AND `code` = 'GROUP'
);

-- 3. 寫入 FUNCTION 功能配置維度
INSERT INTO `setting_category` (`tenant`, `code`, `name`, `description`, `priority_no`, `active_flag`, `created_at`, `updated_at`)
SELECT 'CW', 'FUNCTION', '功能配置', '前端功能與頁面模組配置維度', 4, 'Y', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `setting_category` WHERE `tenant` = 'CW' AND `code` = 'FUNCTION'
);

-- ========================================================
-- 階段二：寫入 setting 業務設定表 (範例基礎參數)
-- ========================================================

-- 寫入連線逾時設定 (ROLE / SESSION)
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
SELECT 'CW', 'ROLE', 'SESSION', 'SESSION_TIMEOUT_MINUTES', '連線逾時分鐘數', '使用者無操作時之登出時間設定', '30', 1, 'Y'
WHERE NOT EXISTS (
    SELECT 1 FROM `setting` WHERE `tenant` = 'CW' AND `category` = 'ROLE' AND `code` = 'SESSION_TIMEOUT_MINUTES'
);

-- 寫入儀表板功能開關 (FUNCTION / DASHBOARD)
INSERT INTO `setting` (`tenant`, `category`, `sub_category`, `code`, `name`, `description`, `value`, `priority_no`, `active_flag`)
SELECT 'CW', 'FUNCTION', 'DASHBOARD', 'DASHBOARD_ANALYTICS', '儀表板數據分析模組', '控制首頁即時分析圖表呈現', 'ENABLED', 1, 'Y'
WHERE NOT EXISTS (
    SELECT 1 FROM `setting` WHERE `tenant` = 'CW' AND `category` = 'FUNCTION' AND `code` = 'DASHBOARD_ANALYTICS'
);
```

---

### 7. 建議推薦配置範本 (Best Practice Template)

| 大分類 (Category) | 子模組 (SubCategory) | 代碼 (Code) | 顯示名稱 (Name) | 數值 (Value) | 說明 (Description) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`ROLE`** | `SESSION` | `MAX_ACTIVE_SESSIONS` | 最大同時登入工作階段數 | `3` | 限制單一帳號並行登入上限 |
| **`ROLE`** | `SESSION` | `SESSION_TIMEOUT_MINUTES` | 連線逾時分鐘數 | `30` | 逾時強制重新驗證 |
| **`ROLE`** | `SECURITY` | `PASSWORD_EXPIRY_DAYS` | 密碼強制變更天數 | `90` | 企業資訊安全密碼週期控管 |
| **`GROUP`** | `DEPARTMENT` | `DEFAULT_MEMBER_LIMIT` | 部門預設人數上限 | `100` | 組織架構群組人數閾值 |
| **`FUNCTION`** | `DASHBOARD` | `DASHBOARD_ANALYTICS` | 儀表板數據分析模組 | `ENABLED` | 控制首頁即時分析圖表呈現 |
| **`FUNCTION`** | `NOTIFICATION`| `LINE_NOTIFY_ENABLED` | LINE 告警即時推播 | `TRUE` | 第三方告警通知服務整合開關 |
| **`FUNCTION`** | `EXPORT` | `MAX_EXPORT_ROWS` | 報表單次匯出最大筆數 | `10000` | 避免大量資料匯出耗盡記憶體 |

---

### 8. 配置成果驗證清單 (Verification Checklist)

完成手動配置後，請依照以下檢核點確認系統狀態：

* [ ] **元資料字典清單驗證**：進入「元資料字典」頁面，表格中應包含 `SYSTEM` 及剛新增的 `ROLE`、`GROUP`、`FUNCTION`。
* [ ] **大分類下拉選單聯動**：進入「功能設定管理」，篩選欄的「大維度 (Category)」下拉選單應呈現 `系統配置 (SYSTEM)`、`角色配置 (ROLE)`、`群組配置 (GROUP)`、`功能配置 (FUNCTION)`。
* [ ] **二級子模組聯動驗證**：在篩選列或新增彈窗中選取 `ROLE` 後，子模組下拉選單應即時聯動顯示 `SESSION`、`SECURITY`。
* [ ] **排序順序驗證**：設定項目清單嚴格依 `priorityNo`（升冪）排列，平局時以 `id` 排序，分頁翻頁無重複或遺漏。
* [ ] **租戶隔離驗證**：所有建立之記錄其租戶欄位均正確記錄為 `CW`。

---

### 9. 常見問題與排查 (FAQ)

1. **問：新增設定時，找不到想要的大分類？**
   - **答**：請先至「**元資料字典**」介面新增該分類維度（例如 `PAYMENT`），儲存後在「功能設定管理」中即刻可用。
2. **問：子模組 (SubCategory) 需要先在字典定義嗎？**
   - **答**：不需要。子模組採開放式設計，在新增業務設定時直接指定名稱即可，系統會自動在該分類下聚合所有不重複的子模組選項。
3. **問：停用維度字典會發生什麼事？**
   - **答**：停用該維度後，新增與編輯設定時的下拉選單將不再顯示該項目，但已存在的歷史設定不會被破壞。
4. **問：如何批次調整設定項目的顯示排序 (Priority No)？**
   - **答**：可調用批次排序端點 `PUT /api/v1/settings/sort`，傳送包含 `id` 與 `priorityNo` 的陣列：
     ```bash
     curl -X PUT http://localhost:8080/api/v1/settings/sort \
       -H "Content-Type: application/json" \
       -H "X-Tenant-Id: CW" \
       -d '[{"id": 10, "priorityNo": 1}, {"id": 9, "priorityNo": 2}]'
     ```
     系統會自動在事務中更新，並保證回傳資料嚴格依照 `priorityNo` 升冪排序。
5. **問：為什麼表格工具列右側沒有看到「範圍：CW」標籤？**
   - **答**：由於全域 `ALL` 機制已全面擱置，系統已透過頂部租戶切換器 (`🏢 租戶 CW 租戶 (CW)`) 提供全局租戶狀態管理，表格工具列已移除該冗餘靜態標籤，介面更為簡潔清爽。

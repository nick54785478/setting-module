# Setting Module (Function Configurable 通用配置與元資料字典模組)

本專案為企業級微服務生態系提供統一、彈性且具備多租戶隔離能力的底層通用配置模組與前後端整合管理平台：
* **`backend/`**：基於 **Spring Boot 4.1.0**、**Maven**、**Java 21**、**Spring Data JPA** 與 **MySQL 8.0**。架構遵循 [AGENTS.md](file:///.agents/AGENTS.md) 之 Clean Architecture / 六角形架構 (Ports & Adapters)，實現「維度元資料字典 (`SettingCategory`)」與「業務功能設定 (`Setting`)」之雙實體徹底解耦。
* **`frontend/`**：基於 **Angular 18**、**PrimeNG 18** (Aura 主題) 與 **TypeScript**。提供響應式 (RWD) 企業級管理後台，支援全域多租戶切換、二級聯動下拉選單、動態 Specification 分頁查詢，以及「功能設定管理」與「元資料字典」雙模組即時視圖導航。

---

## 📁 專案目錄結構

```text
setting-module/
├── backend/                   # Spring Boot 4 後端模組
│   ├── src/
│   │   ├── main/java/com/module/setting/
│   │   │   ├── application/   # 應用層 (Use Cases, Ports, CQRS, Commands, ContextHolder)
│   │   │   ├── infrastructure/# 基礎設施層 (JPA Entities, Repositories, Adapters, Specs)
│   │   │   │   ├── persistence/category/  # SettingCategory 維度字典實體與倉儲
│   │   │   │   ├── persistence/setting/   # Setting 業務設定實體、VO 與倉儲
│   │   │   │   └── adapter/               # SettingRepositoryAdapter
│   │   │   ├── presentation/  # 表現層 (REST Controllers, Resources, Filter)
│   │   │   │   ├── controller/            # SettingController, SettingCategoryController
│   │   │   │   ├── resource/              # REST Request / Response DTOs
│   │   │   │   └── filter/                # TenantContextFilter (多租戶穿透攔截器)
│   │   │   └── config/        # 配置層 (OpenAPI, CORS, Logback, SettingDataInitializer)
│   │   └── resources/         # 配置資源檔 (application.yml, logback-spring.xml)
│   ├── pom.xml                # Maven 依賴管理 (SpringDoc, MySQL, H2, Lombok, UTF-8 配置)
│   └── mvnw.cmd
├── frontend/                  # Angular 18 + PrimeNG 前端管理專案
│   ├── src/app/
│   │   ├── core/              # 核心佈局與全域服務 (Layout, Header, Sidebar, NavigationService, TenantService)
│   │   ├── features/          # 業務功能模組
│   │   │   ├── setting/       # 功能設定管理 (SettingManagementComponent, SettingService)
│   │   │   └── category/      # 元資料字典維護 (CategoryManagementComponent, CategoryService)
│   │   ├── app.component.ts   # 應用外殼 (視圖動態切換容器)
│   │   └── app.component.html # 佈局渲染
│   ├── proxy.conf.json        # 本機反向代理 (/api -> http://localhost:8080)
│   ├── package.json           # Angular 18, PrimeNG 18, PrimeIcons 7
│   └── angular.json
├── doc/                       # 系統核心架構設計規格與維運文件
│   ├── design/
│   │   └── setting-architecture-sd.md  # 統一權威全景系統設計規格書 (v2.0.0)
│   ├── guide/
│   │   └── manual-setting-configuration-guide.md # 系統設定與維度字典手動配置作業手冊
│   └── BACKLOG.md             # 專案需求與技術債儲備清單 (BL-001)
├── .agents/                   # 架構與前後端開發規範準則 (AGENTS.md)
└── README.md
```

---

## 🚀 快速啟動指南

### 1. 啟動後端服務 (`backend`)
確認本機 MySQL 8.0 容器運作中（`docker compose up -d`），於 IDE 執行 `backend/src/main/java/com/module/setting/SettingModuleApplication.java`，或使用 Maven 啟動：
```bash
cd backend
./mvnw spring-boot:run
```
* 後端服務位址：[http://localhost:8080](http://localhost:8080)
* Swagger UI 互動式文件：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* OpenAPI 3 JSON 規範：[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 2. 啟動前端介面 (`frontend`)
```bash
cd frontend
npm install
npm start
```
* 前端管理介面：[http://localhost:4200](http://localhost:4200)

---

## 📚 系統架構與設計文件 (System Design Docs)

* 📘 **統一架構設計規格書**：[doc/design/setting-architecture-sd.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/design/setting-architecture-sd.md)  
  *完整整併資料模型、DDL 索引、雙端點二級聯動 Sequence Diagram、純淨初始化展開與多租戶穿透機制（v2.0.0 權威單一真理源）。*
* 📙 **維運手動配置操作手冊**：[doc/guide/manual-setting-configuration-guide.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/guide/manual-setting-configuration-guide.md)  
  *指導維運人員於系統初始化後，如何手動建立 `ROLE`、`GROUP`、`FUNCTION` 等維度及具體業務功能設定。*
* 📌 **專案需求與技術儲備清單**：[doc/BACKLOG.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/BACKLOG.md)  
  *記錄未來擴充需求，包含 BL-001 多租戶動態切換器與開通展開機制。*

---

## 🏛️ 領域與資料模型 (雙實體解耦架構)

系統徹底解耦「大維度字典」與「業務設定項目」，杜絕過往將 `META` 元資料塞在業務表所造成的選單混雜與假資料污染：

```
[setting_category 表] ── 1. 定義大維度 ──> (SYSTEM, ROLE, GROUP, FUNCTION)
                                                 │
                                         2. 坐標關聯約束 (scope.category)
                                                 ▼
[setting 表]          ── 3. 儲存具體參數 ─> (SERVER_PORT, MAX_CONNECTIONS...)
```

### 1. `SettingCategory` (大維度字典實體，`setting_category` 表)
* `id`: 流水號主鍵
* `tenant`: 租戶識別碼（`ALL` 代表全域共用）
* `code`: 維度唯一代碼（如 `SYSTEM`, `ROLE`, `GROUP`, `FUNCTION`）
* `name`: 中文顯示名稱（如 `系統配置`, `角色配置`）
* `description`: 維度詳細說明
* `priorityNo`: 排序序號
* `activeFlag`: 有效性標記（`YesNo.Y` / `YesNo.N`，支援停用）
* **約束與索引**：`uk_setting_category_tenant_code (tenant, code)`、`idx_category_tenant_active`

### 2. `Setting` (核心業務設定實體，`setting` 表)
* `id`: 流水號主鍵
* `@Embedded SettingScope scope`: 四維定位坐標
  * `tenant`: 租戶識別碼（如 `ALL`, `T001`）
  * `category`: 業務功能維度（參照 `SettingCategory.code`）
  * `subCategory`: 業務子模組 / 分類（如 `SERVER`, `DATABASE`, `SESSION`）
  * `code`: 設定唯一識別代碼（如 `SERVER_PORT`, `MAX_SESSIONS`）
* `@Embedded SettingProfile profile`: 展示屬性
  * `name`: 顯示標籤 (前端 Label)
  * `description`: 功能用途詳細說明
* `value`: 具體配置值（如 `8080`, `100`）
* `priorityNo`: 排序順序號
* `activeFlag`: 有效性標記（`YesNo.Y` / `YesNo.N`，支援邏輯刪除）
* **約束與索引**：`uk_setting_scope (tenant, category, code)`、`idx_tenant_category_priority`、`idx_tenant_category_sub_priority`、`idx_tenant_active`

---

## 📡 RESTful API 規格全覽

所有 API 均支援透過 Request Header `X-Tenant-Id: <tenant>` 自動帶入租戶識別碼，亦可於 Query Param 指定。

### 1. 元資料字典維護 API (`SettingCategoryController`)

| HTTP 方法 | RESTful 路徑 | 說明 | 查詢參數 / 請求內容 | 回應型別 |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/categories` | 取得維度字典清單 | `tenant` (可選), `includeInactive` (可選) | `List<CategoryGottenResource>` |
| `POST` | `/api/v1/categories` | 新增維度字典項目 | `CreateCategoryResource` (JSON) | `CategoryGottenResource` |
| `PUT` | `/api/v1/categories/{id}` | 更新現有維度字典 | `UpdateCategoryResource` (JSON) | `CategoryGottenResource` |
| `DELETE` | `/api/v1/categories/{id}` | 標記停用維度字典 (`activeFlag='N'`) | - | 204 No Content |

### 2. 功能設定管理 API (`SettingController`)

| HTTP 方法 | RESTful 路徑 | 說明 | 查詢參數 / 請求內容 | 回應型別 |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/settings` | 建立新業務設定 | `CreateSettingResource` (JSON) | `SettingCreatedResource` |
| `PUT` | `/api/v1/settings/{id}` | 更新現有業務設定 | `UpdateSettingResource` (JSON) | `SettingUpdatedResource` |
| `GET` | `/api/v1/settings/{id}` | 依 ID 取得單筆設定 | - | `SettingGottenResource` |
| `GET` | `/api/v1/settings/summary` | 多條件動態分頁查詢 (Specification) | `category`, `subCategory`, `code`, `name`, `activeFlag`, `page`, `size` | `SettingPagedGottenResource` |
| `GET` | `/api/v1/settings/categories` | 取得大分類下拉選單清單 | `tenant` (可選) | `List<SettingOptionGottenResource>` |
| `GET` | `/api/v1/settings/categories/{cat}/sub-categories` | 依大分類取得真實子模組下拉選單 | `cat` (路徑變數), `tenant` (可選) | `List<SettingOptionGottenResource>` |
| `DELETE` | `/api/v1/settings/{id}` | 邏輯刪除設定 (`activeFlag='N'`) | - | 204 No Content |

---

## 🎨 前端架構與特色功能 (Frontend Architecture)

* **現代化元件分離規範**：嚴格遵循 `.agents/frontend/AGENTS.md`，每個元件獨立維護 `.ts`、`.html` 與 `.scss`。
* **雙功能視圖動態切換**：
  * **功能設定管理**：提供企業級藍色高密度表格、二級聯動下拉選單、多維度即時篩選、分頁器與新增/編輯 Modal。
  * **元資料字典維護**：專門提供大維度字典之獨立列表、排序設定與啟用/停用管理。
* **無縫導航與狀態同步**：
  * `NavigationService` 集中管理視圖狀態，點選側邊欄 ([SidebarComponent](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/components/sidebar/sidebar.component.ts)) 時，主視圖與頁首麵包屑導航 ([HeaderComponent](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/components/header/header.component.ts)) 即時同步切換。
* **響應式斷點設計 (RWD)**：支援桌機 (1280px+)、平板 (992px) 與手機螢幕 (640px / 420px)，具備側邊欄滑動收合與對話框全寬自適應。

---

## 🛡️ 純淨初始化與資料無污染原則 (Zero-Pollution Baseline)

* **`setting_category` 表**：首次啟動時僅寫入唯一一筆全域基礎設施維度 `SYSTEM`（系統配置）。
* **`setting` 表**：初始化筆數嚴格為 **0 筆**，不注入任何業務假資料。
* **手動配置**：若需使用 `ROLE`、`GROUP`、`FUNCTION` 等維度或新增設定，請依照 [manual-setting-configuration-guide.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/guide/manual-setting-configuration-guide.md) 手動建立。
* **控制台編碼保護**：專案已內建 [logback-spring.xml](file:///d:/桌面/ModuleWorkSpace/setting-module/backend/src/main/resources/logback-spring.xml) 與 Maven UTF-8 啟動參數，杜絕 Windows 終端中文亂碼問題。

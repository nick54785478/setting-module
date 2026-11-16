# Setting Module (Function Configurable 通用配置與元資料字典模組)

本專案為企業級微服務生態系提供統一、彈性且具備多租戶隔離能力的底層通用配置模組與前後端整合管理平台：
* **`backend/`**：基於 **Spring Boot 4.1.0**、**Maven**、**Java 21**、**Spring Data JPA** 與 **MySQL 8.0**。架構遵循 [AGENTS.md](file:///.agents/AGENTS.md) 之 Clean Architecture / 六角形架構 (Ports & Adapters)，實現「維度元資料字典 (`SettingCategory`)」與「業務功能設定 (`Setting`)」之雙實體徹底解耦。
* **`frontend/`**：基於 **Angular 18**、**PrimeNG 18** (Aura 主題) 與 **TypeScript**。提供響應式 (RWD) 企業級管理後台，支援全域多租戶與使用者上下文穿透 (`X-Tenant-Id: CW`, `X-Username: nickgh.zhang@cw.com`)、二級聯動下拉選單、動態 Specification 分頁查詢，以及「功能設定管理」與「元資料字典」雙模組即時視圖導航。

---

## 💡 什麼是 Function Configurable？(核心理念與價值)

### 1. 核心定位
**「Function Configurable（功能與參數動態配置）」** 是本模組的核心使命：
> **將全系統中所有的「業務功能開關（Feature Flags）」、「運行參數與閾值（Runtime Parameters）」、「頁面路由與端點（Dynamic Routes）」與「組織/角色策略」，從傳統的硬編碼（Hardcode）或重啟才能生效的設定檔（`application.yml`）中徹底抽離，轉化為可在系統執行時期（Runtime）透過管理介面即時動態配置、即時生效，且具備多租戶隔離性的標準化中樞。**

各業務微服務不再需要各自重複建立 `sys_config` 或 `app_param` 資料表，直接介接本模組即可享有開箱即用的動態配置管理能力。

### 2. 傳統開發痛點 vs. Function Configurable

| 比較項目 | 傳統做法（非 Configurable） | Function Configurable（本模組做法） |
| :--- | :--- | :--- |
| **變更生效機制** | 修改程式碼或 yml -> 重新編譯、打包部署、重啟微服務 | **管理後台修改儲存 -> 秒級即時生效（Zero Downtime，免重啟）** |
| **多租戶自適應** | 在業務程式中撰寫繁瑣的 `if (tenant == 'CW')` 分支 | **天然支援 `tenant` 空間隔離，各租戶配置獨立互不干擾** |
| **微服務維護成本** | 各團隊各自為政，重複開闢設定表與後台維護頁面 | **全企業共用標準四維坐標模型，提供統一 REST API 與 UI 後台** |
| **架構字典管理** | 將元資料塞入業務表，造成冷啟動空白與選單錯亂 | **雙實體解耦（`SettingCategory` 字典 + `Setting` 業務設定）** |

*詳細架構概念與白皮書請參閱：[doc/design/function-configurable-concept.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/design/function-configurable-concept.md)*

---

## 📁 專案目錄結構

```text
setting-module/
├── backend/                   # Spring Boot 4 後端模組
│   ├── src/
│   │   ├── main/java/com/module/setting/
│   │   │   ├── application/   # 應用層 (Use Cases, Inbound/Outbound Ports, CQRS Commands/Results)
│   │   │   │   └── shared/context/    # UserContextHolder (ThreadLocal 儲存 Tenant 與 Username)
│   │   │   ├── infrastructure/# 基礎設施層 (JPA Entities, Repositories, Adapters, Specs)
│   │   │   │   ├── persistence/category/  # SettingCategory 維度字典實體與倉儲
│   │   │   │   ├── persistence/setting/   # Setting 業務設定實體、VO 與倉儲
│   │   │   │   └── adapter/               # SettingRepositoryAdapter
│   │   │   ├── presentation/  # 表現層 (REST Controllers, Resources, Assemblers, Filters)
│   │   │   │   ├── controller/            # SettingController, SettingCategoryController
│   │   │   │   ├── resource/              # REST Request (*Resource) / Response (*Resource)
│   │   │   │   ├── assembler/             # SettingResourceAssembler, CategoryResourceAssembler
│   │   │   │   └── filter/                # UserContextFilter (X-Tenant-Id 與 X-Username 標頭解析)
│   │   │   └── config/        # 配置層 (OpenAPI, CORS, Logback, SettingDataInitializer)
│   │   └── resources/         # 配置資源檔 (application.yml, logback-spring.xml)
│   ├── pom.xml                # Maven 依賴管理 (SpringDoc, MySQL, H2, Lombok, UTF-8 配置)
│   └── mvnw.cmd
├── frontend/                  # Angular 18 + PrimeNG 前端管理專案
│   ├── src/app/
│   │   ├── core/              # 核心佈局與全域服務
│   │   │   ├── components/    # HeaderComponent, SidebarComponent
│   │   │   ├── services/      # TenantService (CW 固定租戶), UserService (固定 nickgh.zhang@cw.com), NavigationService
│   │   │   └── interceptors/  # tenantInterceptor (自動注入 X-Tenant-Id 與 X-Username)
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
│   │   ├── function-configurable-concept.md # Function Configurable 核心架構概念白皮書
│   │   └── setting-architecture-sd.md       # 統一權威全景系統設計規格書 (v2.0.0)
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
mvn spring-boot:run
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

* 💡 **核心概念架構白皮書**：[doc/design/function-configurable-concept.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/design/function-configurable-concept.md)  
  *深入解析 Function Configurable 核心定義、傳統痛點對比、四維空間坐標與四大典型業務應用場景。*
* 📘 **統一架構設計規格書**：[doc/design/setting-architecture-sd.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/design/setting-architecture-sd.md)  
  *完整整併資料模型、DDL 索引、雙端點二級聯動 Sequence Diagram、純淨初始化展開與多租戶穿透機制（v2.0.0 權威單一真理源）。*
* 📙 **維運手動配置操作手冊**：[doc/guide/manual-setting-configuration-guide.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/guide/manual-setting-configuration-guide.md)  
  *指導維運人員於系統初始化後，如何手動建立 `ROLE`、`GROUP`、`FUNCTION` 等維度及具體業務功能設定。*
* 📊 **表格欄位個人化顯示配置規格書**：[doc/design/table-column-customisation-design.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/design/table-column-customisation-design.md)  
  *定義使用者表格欄位顯示/隱藏與順序之個人化自訂機制、Customisation 領域模型、DDL 與前後端整合規範。*
* 📌 **專案需求與技術儲備清單**：[doc/BACKLOG.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/BACKLOG.md)  
  *記錄未來擴充需求，包含 BL-001 多租戶動態切換器與展開機制、BL-004 多租戶語系 (Locale / i18n) 後端管理與動態切換機制等。*

---

## 🏛️ 領域與資料模型 (四維定位與雙實體解耦)

系統徹底解耦「大維度字典」與「業務設定項目」，杜絕過往將 `META` 元資料塞在業務表所造成的選單混雜與假資料污染：

```
[setting_category 表] ── 1. 定義大維度 ──> (SYSTEM, ROLE, GROUP, FUNCTION)
                                                 │
                                         2. 坐標關聯約束 (scope.category)
                                                 ▼
[setting 表]          ── 3. 儲存具體參數 ─> (SERVER_PORT, DASHBOARD_ANALYTICS...)
```

### 1. `SettingCategory` (大維度字典實體，`setting_category` 表)
* `id`: 流水號主鍵
* `tenant`: 租戶識別碼（`ALL` 代表全域共用）
* `code`: 維度唯一代碼（如 `SYSTEM`, `ROLE`, `GROUP`, `FUNCTION`）
* `name`: 中文顯示名稱（如 `系統配置`, `角色配置`）
* `description`: 維度詳細說明
* `priorityNo`: 排序序號
* `activeFlag`: 有效性標記（`YesNo.Y` / `YesNo.N`，支援停用）
* **約束與索引**：`uk_setting_category_tenant_code (tenant, code)`、`idx_category_tenant_active_priority`、`idx_category_priority`

### 2. `Setting` (核心業務設定實體，`setting` 表)
* `id`: 流水號主鍵
* `@Embedded SettingScope scope`: 四維定位坐標
  * `tenant`: 租戶識別碼（如 `CW`, `ALL`）
  * `category`: 業務功能維度（參照 `SettingCategory.code`）
  * `subCategory`: 業務子模組 / 分類（如 `SERVER`, `DASHBOARD`, `SESSION`）
  * `code`: 設定唯一識別代碼（如 `SERVER_PORT`, `DASHBOARD_ANALYTICS`）
* `@Embedded SettingProfile profile`: 展示屬性
  * `name`: 顯示標籤 (前端 Label)
  * `description`: 功能用途詳細說明
* `value`: 具體配置值（如 `8080`, `true`, `/analytics`）
* `priorityNo`: 排序順序號（支援升冪精準排序）
* `activeFlag`: 有效性標記（`YesNo.Y` / `YesNo.N`，支援邏輯刪除）
* **約束與索引**：`uk_setting_scope (tenant, category, code)`、`idx_tenant_category_priority`、`idx_tenant_active_priority`、`idx_priority_no`

---

## 📡 RESTful API 規格全覽

所有 API 均支援透過 Request Header `X-Tenant-Id: <tenant>` 與 `X-Username: <username>` 自動帶入上下文識別碼。

### 1. 元資料字典維護 API (`SettingCategoryController`)

| HTTP 方法 | RESTful 路徑 | 說明 | 查詢參數 / 請求內容 | 回應型別 |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/categories` | 取得維度字典清單 | `tenant` (可選), `includeInactive` (可選) | `List<CategoryGottenResource>` |
| `POST` | `/api/v1/categories` | 新增維度字典項目 | `CreateCategoryResource` (JSON) | `CategoryCreatedResource` |
| `PUT` | `/api/v1/categories/{id}` | 更新現有維度字典 | `UpdateCategoryResource` (JSON) | `CategoryUpdatedResource` |
| `GET` | `/api/v1/categories/{id}` | 依 ID 取得單筆維度字典 | - | `CategoryGottenResource` |
| `DELETE` | `/api/v1/categories/{id}` | 標記停用維度字典 (`activeFlag='N'`) | - | 204 No Content |

### 2. 功能設定管理 API (`SettingController`)

| HTTP 方法 | RESTful 路徑 | 說明 | 查詢參數 / 請求內容 | 回應型別 |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/settings` | 建立新業務設定 | `CreateSettingResource` (JSON) | `SettingCreatedResource` |
| `PUT` | `/api/v1/settings/{id}` | 更新現有業務設定 | `UpdateSettingResource` (JSON) | `SettingUpdatedResource` |
| `GET` | `/api/v1/settings/{id}` | 依 ID 取得單筆設定 | - | `SettingGottenResource` |
| `GET` | `/api/v1/settings/summary` | 多條件動態分頁查詢 (Specification，依 `priorityNo` 排序) | `category`, `subCategory`, `code`, `name`, `activeFlag`, `page`, `size` | `SettingPagedGottenResource` |
| `GET` | `/api/v1/settings/categories` | 取得大分類下拉選單清單 | `tenant` (可選) | `List<SettingOptionGottenResource>` |
| `GET` | `/api/v1/settings/categories/{cat}/sub-categories` | 依大分類取得真實子模組下拉選單 | `cat` (路徑變數), `tenant` (可選) | `List<SettingOptionGottenResource>` |
| `DELETE` | `/api/v1/settings/{id}` | 邏輯刪除設定 (`activeFlag='N'`) | - | 204 No Content |

---

## 🎨 前端架構與特色功能 (Frontend Architecture)

* **現代化元件分離規範**：嚴格遵循 `.agents/frontend/AGENTS.md`，每個元件獨立維護 `.ts`、`.html` 與 `.scss`。
* **雙功能視圖動態切換**：
  * **功能設定管理**：提供企業級藍色高密度表格、二級聯動下拉選單、多維度即時篩選、分頁器與新增（支援手動新增子模組）/編輯 Modal。
  * **元資料字典維護**：專門提供大維度字典之獨立列表、排序設定與啟用/停用管理。
* **無縫導航與狀態同步**：
  * `NavigationService` 集中管理視圖狀態，點選側邊欄 ([SidebarComponent](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/components/sidebar/sidebar.component.ts)) 時，主視圖與頁首麵包屑導航 ([HeaderComponent](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/components/header/header.component.ts)) 即時同步切換。
* **多租戶與使用者上下文穿透**：
  * 前端 [tenantInterceptor](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/interceptors/tenant.interceptor.ts) 自動為所有請求注入 `X-Tenant-Id: CW` 與 `X-Username: nickgh.zhang@cw.com`，頁首即時展示當前登入者資訊。
* **響應式斷點設計 (RWD)**：支援桌機 (1280px+)、平板 (992px) 與手機螢幕 (640px / 420px)，具備側邊欄滑動收合與對話框全寬自適應。

---

## 🛡️ 純淨初始化與資料無污染原則 (Zero-Pollution Baseline)

* **`setting_category` 表**：首次啟動時僅寫入唯一一筆全域基礎設施維度 `SYSTEM`（系統配置）。
* **`setting` 表**：初始化筆數嚴格為 **0 筆**，不注入任何業務假資料。
* **手動配置**：若需使用 `ROLE`、`GROUP`、`FUNCTION` 等維度或新增設定，請依照 [manual-setting-configuration-guide.md](file:///d:/桌面/ModuleWorkSpace/setting-module/doc/guide/manual-setting-configuration-guide.md) 手動建立。
* **控制台編碼保護**：專案已內建 [logback-spring.xml](file:///d:/桌面/ModuleWorkSpace/setting-module/backend/src/main/resources/logback-spring.xml) 與 Maven UTF-8 啟動參數，杜絕 Windows 終端中文亂碼問題。

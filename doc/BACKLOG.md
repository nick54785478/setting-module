# 專案需求與技術儲備清單 (Product & Technical Backlog)

本文件作為 `setting-module` 專案之產品功能擴充與技術架構優化儲備清單（Backlog），記錄未來迭代規劃與待落實項目。

---

## 📌 需求狀態圖例 (Legend)
* 🟢 **Done**：已完成並驗證通過
* 🟡 **In Progress**：實作開發中
* ⚪ **Backlog**：待排程規劃

---

## 📋 待辦事項清單 (Backlog Items)

### [BL-001] 多租戶動態切換與彈性租戶配置擴充 (Dynamic Multi-Tenant Switcher & Provisioning)

* **項目編號**：`BL-001`
* **優先等級**：`High`
* **所屬領域**：前端 Core 佈局 / 後端多租戶架構
* **狀態**：⚪ **Backlog (規劃中)**
* **建立日期**：2026-11-16

#### 1. 背景與現況說明 (Background & Current State)
* **現階段實作**：
  依據現行商業階段規劃，前端介面將租戶識別碼（Tenant）**暫時固定寫死為 `CW` 租戶**。
  - 前端 [TenantService.ts](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/services/tenant.service.ts) 預設鎖定為 `CW`。
  - 頂部導航欄 [HeaderComponent](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/components/header/header.component.ts) 的租戶下拉切換器設為唯讀鎖定。
  - 新增設定與維度字典時，表單之租戶欄位預設為 `CW` 且不可編輯。
  - 後端已具備多租戶穿透與過濾基礎設施（透過 `X-Tenant-Id` 與 `X-Username` 請求標頭、`UserContextFilter` 與 `UserContextHolder` 自動注入）。

#### 2. 擴充目標與驗收條件 (Objectives & Acceptance Criteria)
未來當系統進一步支援多租戶商用營運時，需落實以下五項能力擴充：

1. **後端租戶元資料字典管理 (`Tenant Registry`)**：
   - 建立獨立的租戶實體 `Tenant`（包含 `tenantCode`、`tenantName`、`status`、`description` 等）。
   - 提供 `/api/v1/tenants` RESTful CRUD API，供後台即時維護租戶清單。
2. **前端動態租戶切換器 (`Dynamic Tenant Switcher`)**：
   - `TenantService` 改造：移除硬編碼 `CW`，改為在應用初始化時向 `/api/v1/tenants` 拉取有效租戶清單。
   - `HeaderComponent` 解除唯讀鎖定，提供即時下拉切換。
   - 記憶化功能：將使用者最後選取的租戶存入 `localStorage`，重新整理後自動保留。
3. **新租戶簽約開通自動展開流程 (`Tenant Onboarding Provisioning`)**：
   - 當新租戶於系統建立時，發布 `TenantCreatedEvent` 事件。
   - 觸發 `InitTenantSettingUseCase`，依據系統基準設定範本自動為該新租戶展開預設設定。
4. **多層級租戶繼承與全域覆寫機制 (`Tenant Inheritance & Fallback`)**：
   - 強化查詢解析器：若特定租戶未自訂某項設定，系統自動向上回退（Fallback）讀取全域 `ALL` 租戶之設定值；若租戶有專屬設定，則優先覆寫。
5. **租戶資料隔離與權限邊界驗證 (`Tenant RBAC & Security Isolation`)**：
   - 整合使用者身分驗證權限（如 JWT Claims），禁止租戶使用者偽造 `X-Tenant-Id` 跨租戶存取其他企業之敏感參數。

---

### [BL-002] 設定變更審計歷史與版本比對 (Setting Audit Trail & Diff)

* **項目編號**：`BL-002`
* **優先等級**：`Medium`
* **狀態**：⚪ **Backlog (規劃中)**
* **說明**：
  - 建立 `setting_history` 歷史軌跡表，每次更新業務設定時記錄變更前後數值、變更人、時間與操作類型。
  - 前端提供設定歷史檢視與 Version Diff 視覺化彈窗。

---

### [BL-003] 分散式快取整合 (Redis Distributed Cache & Invalidation)

* **項目編號**：`BL-003`
* **優先等級**：`Medium`
* **狀態**：⚪ **Backlog (規劃中)**
* **說明**：
  - 針對高頻率讀取的系統設定（如 `SERVER_PORT`, `MAX_CONNECTIONS` 等），於應用層加入 Redis 快取支援。
  - 當設定被 `PUT` 或 `DELETE` 時，自動觸發 Redis 快取失效（Cache Eviction）與微服務廣播更新。

---

### [BL-004] 多租戶語系 (Locale / i18n) 後端管理與動態切換機制 (Dynamic Multi-Tenant Locale & i18n Management)

* **項目編號**：`BL-004`
* **優先等級**：`High`
* **所屬領域**：前端 Core 佈局 / 後端多語系與租戶架構
* **狀態**：⚪ **Backlog (規劃中)**
* **建立日期**：2026-11-16

#### 1. 背景與商業價值 (Background & Business Value)
* **避免前端發版成本**：
  傳統前端多語系大多將 JSON/YAML 靜態檔案打包於前端 Bundle 中，任何詞條修訂、翻譯優化或新增語系都必須重新執行前端建置與發版部署流程。由後端動態管理可達成「免發版、即時更新」。
* **多租戶白牌文案客製 (White-label & Custom Terminology)**：
  在 B2B 多租戶 SaaS 環境中，不同企業租戶常有專屬的術語習慣（例如：同樣的欄位，A 租戶稱為「工號角色」，B 租戶稱為「職務權限」）。透過後端資料庫層級的租戶隔離，可為不同租戶提供專屬客製化詞條。
* **前後端一致的錯誤與訊息國際化**：
  後端業務校驗、例外處理（Exception Response）能與前端使用同一套語系上下文，避免後端吐英文/繁中但前端顯示簡中之語言混雜問題。
* **跨裝置使用者偏好持久化**：
  使用者的語系偏好儲存於後端資料庫，更換裝置或瀏覽器登入時自動載入其個人偏好。

#### 2. 架構設計與運作流程 (Technical Architecture & Flow)
1. **前端導航欄 Header 語系選擇器**：
   - 頂部導航欄 [HeaderComponent](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/components/header/header.component.ts) 新增語系下拉選單（例如：`繁體中文 (zh-TW)`、`English (en-US)`、`简体中文 (zh-CN)`）。
   - 使用者切換語系時，即時更新 `UserService` 與本地 `localStorage`，並通知 i18n 載入器載入對應語言套件。
2. **HTTP 請求語系標頭穿透 (Request Header Propagation)**：
   - 前端攔截器 [tenant.interceptor.ts](file:///d:/桌面/ModuleWorkSpace/setting-module/frontend/src/app/core/interceptors/tenant.interceptor.ts) 統一注入語系請求標頭：
     ```http
     Accept-Language: zh-TW
     X-Locale: zh-TW
     ```
3. **後端 Context 攔截與解析**：
   - 後端 [UserContextFilter](file:///d:/桌面/ModuleWorkSpace/setting-module/backend/src/main/java/com/module/setting/application/shared/filter/UserContextFilter.java) 攔截 `X-Locale` 或 `Accept-Language` 標頭。
   - 解析後存入 [UserContextHolder](file:///d:/桌面/ModuleWorkSpace/setting-module/backend/src/main/java/com/module/setting/application/shared/context/UserContextHolder.java) 的 `locale` 變數（提供 `UserContextHolder.getLocale()` 供後續業務使用）。
4. **現有 Setting 模組的詞條模型複用 (Setting Model Reuse)**：
   - **無需新增複雜專用表**，直接利用本系統的 `setting` 表結構管理詞條：
     - `category = 'LOCALE'`
     - `subCategory` 代表語系代碼（如 `zh-TW`, `en-US`, `zh-CN`）
     - `code` 為多語系鍵（如 `SETTING.CREATE.TITLE`, `COMMON.BUTTON.SAVE`）
     - `value` 為翻譯文本（如 `新增功能設定項目`, `確認儲存`）
     - `tenant` 欄位支援租戶專屬覆寫（若特定租戶未定義，則 Fallback 回退至全域 `ALL` 租戶的預設詞條）。

#### 3. 分階段實作規劃 (Implementation Phases)
* **Phase 1：基礎架構與語系穿透**
  - [ ] 後端 `UserContextHolder` 擴充 `locale` 欄位與 Getter/Setter，支援預設回退至 `Locale.TRADITIONAL_CHINESE`。
  - [ ] 後端 `UserContextFilter` 新增 `X-Locale` / `Accept-Language` 標頭讀取邏輯並加入單元測試。
  - [ ] 前端 `UserService` 增加 `locale` 狀態管理（Signal）。
  - [ ] 前端 `HeaderComponent` 新增語系選擇 UI 下拉選單。
  - [ ] 前端 `tenant.interceptor.ts` 自動附加 `X-Locale` 標頭。
* **Phase 2：後端詞條批次 API 與前端動態載入**
  - [ ] 後端提供 `/api/v1/settings/locales/bundle` 端點，支援依 `tenant` 與 `locale` 批次查詢所有語系字典 Key-Value Map。
  - [ ] 支援多租戶詞條繼承覆寫邏輯（`Tenant Setting` > `Global 'ALL' Setting`）。
  - [ ] 前端整合動態 Translate Service，於初始化或切換語系時拉取字典包，支援畫面即時熱切換。
* **Phase 3：快取效能優化與線上詞條管理**
  - [ ] 後端加入 HTTP ETag 或 Redis 快取，若語系字典未變更則回傳 `304 Not Modified`，避免重複傳輸。
  - [ ] 於本模組 UI 建立 `LOCALE` 維度，業務管理員可直接在線上修改介面詞條，隨改隨生效。

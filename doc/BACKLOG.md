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
  - 後端已具備多租戶穿透與過濾基礎設施（透過 `X-Tenant-Id` 請求標頭、`TenantContextFilter` 與 `TenantContextHolder` 自動注入）。

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

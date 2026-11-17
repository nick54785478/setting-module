# 租戶系統語系配置與切換維護 (Tenant Locale Management) 設計文件

## 一、需求背景與設計理念

在多租戶雲端架構下，不同企業租戶（Tenant）所營運的跨國業務語系各不相同：
- 租戶 A 可能僅需要 `繁體中文 (zh-TW)` 與 `English (en-US)`。
- 租戶 B 可能需要 `日本語 (ja-JP)`、`English (en-US)` 與 `한국어 (ko-KR)`。
- 各租戶需能自定義預設語系（Default Locale）與語系排序。

### 為什麼採用方案 A（Function Configurable）？
本模組貫徹 **Function Configurable** 之核心原則——**不為單一設定類型開闢專屬表，而是善用通用多維坐標體系 (`SettingScope`) 進行配置管理**：
1. **維度元資料字典 (`SettingCategory`)**：註冊基礎維度 `LOCALE`（系統語系配置）。
2. **租戶專屬語系 (`Setting`)**：
   - `scope.tenant`: 當前租戶代碼（例如 `CW`, `ENTERPRISE_A`；各租戶獨立定義，無全域 ALL 回退）。
   - `scope.category`: 固定為 `"LOCALE"`。
   - `scope.code`: 語系代碼（例如 `zh-TW`, `en-US`, `ja-JP`）。
   - `profile.name`: 顯示名稱（例如 `"繁體中文"`, `"English"`, `"日本語"`）。
   - `value`: 標記預設語系（`"DEFAULT"` 代表該租戶預設語系）。
   - `priorityNo`: 語系在選單上的排序權重。
   - `activeFlag`: 該租戶是否啟用此語系 (`Y` / `N`)。

---

## 二、六角架構與後端 RESTful API

### 1. 目錄結構
```text
backend/src/main/java/com/module/setting/
├── config/init/SettingDataInitializer.java            # 自動註冊 LOCALE 維度與 CW 基準展示語系
├── presentation/
│   ├── controller/SettingController.java              # 提供 /api/v1/settings/locales 等專用端點
│   └── resource/out/TenantLocaleOptionGottenResource.java   # 語系選項 Resource
```

### 2. RESTful API 規格
| HTTP Method | Endpoint | 說明 |
| :--- | :--- | :--- |
| `GET` | `/api/v1/settings/locales` | 取得當前租戶已啟用之系統語系列表（各租戶獨立，無全域回退） |
| `GET` | `/api/v1/settings/locales/all` | 取得當前租戶所有語系列表（含停用，供管理彈窗使用） |
| `PUT` | `/api/v1/settings/locales/{id}/default` | 將指定語系設為該租戶之預設語系（清空舊預設標記） |
| `POST` | `/api/v1/settings` | 新增租戶語系（傳入 `category="LOCALE"`, `code="ja-JP"` 等） |
| `PUT` | `/api/v1/settings/{id}` | 更新租戶語系名稱、排序或停用/啟用 |
| `DELETE` | `/api/v1/settings/{id}` | 邏輯刪除租戶語系 |

---

## 三、前端互動與使用者體驗 (UI / UX)

### 1. 頂部導航列 (HeaderComponent)
- **位置**：位於租戶下拉切換器右側。
- **地球按鈕 (Globe Icon)**：
  - 即時顯示當前選取的系統語系標籤（例如 `🌐 繁體中文`）。
  - 點擊彈出 `p-overlayPanel` 下拉選單。
- **語系切換 Popover**：
  - 動態列出當前租戶已啟用的所有語系。
  - 當前語系帶有藍色勾選標記，預設語系附帶 `[預設]` 標籤。
  - 點選任一語系立即切換系統語系、觸發 Toast 提示，並存入 `localStorage` 記憶。
  - 底部提供「⚙️ 維護租戶語系」按鈕。

### 2. 租戶語系維護彈窗 (p-dialog)
- **功能特色**：
  - 標題動態顯示當前操作租戶：`維護租戶支援語系 (Tenant: CW)`。
  - 表格展示該租戶所有已登記語系，可即時「設為預設」、編輯或刪除。
  - 提供快速常用範本下拉選單（`zh-TW`、`zh-CN`、`en-US`、`ja-JP`、`ko-KR`），一鍵帶入表單。
  - 支援表單自訂語系代碼、顯示名稱、排序號與「設為預設」核取方塊。

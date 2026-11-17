# 租戶語系自定義配置手冊與 Tenant 格式規範
(Tenant Locale Configuration Guide & Specification)

---

## 一、Tenant（租戶）概念與格式規範

### 1.1 什麼是 Tenant？
在多租戶（Multi-Tenancy）雲端軟體架構中，**Tenant（租戶）** 代表系統中的一個獨立組織、企業客戶或營運主體。
本系統基於 **Function Configurable** 架構，實施完全嚴格的租戶數據隔離（Tenant Data Isolation）：
- **各自獨立**：每個租戶擁有完全獨立的業務參數與支援語系列表，互不干擾。
- **無全域回退**：已徹底移除全域 `ALL` 語系預設機制，避免跨租戶語系污染，讓各企業租戶 100% 自主定義所需的系統國際化（i18n）語系集合。

---

### 1.2 Tenant 識別碼格式規範 (Tenant Code Specification)

在資料庫與 API 互動中，`tenant` 欄位為租戶的唯一識別碼，格式約束如下：

| 項目 | 規範標準 | 說明與範例 |
| :--- | :--- | :--- |
| **資料類型** | `VARCHAR(64)` | 於資料庫實體中對應 `String`，最大長度為 64 字元。 |
| **字元限制** | `[A-Z0-9_-]+` | 建議僅使用**大寫英文字母**、**數字**、**底線 (`_`)** 或**連字號 (`-`)**。 |
| **空白規則** | 嚴禁空白 | 不得為 `null`、空字串 `""`，亦不可包含空格或任何換行字元。 |
| **大小寫敏感** | 建議全大寫 | 後端代碼比對時採精確字串比對，全大寫可避免跨資料庫大小寫差異。 |
| **命名慣例 (範例)** | 語意明確的大寫簡寫 | - `CW`：預設展示租戶<br>- `CORP_HQ`：企業總部租戶<br>- `BRANCH_TW`：台灣分公司租戶<br>- `RETAIL_APAC`：亞太零售租戶 |

---

## 二、Function Configurable 語系架構坐標模型

本模組堅持不開闢多餘的獨立硬編碼表，而是依循標準的 **SettingScope 四維坐標** 進行語系收納：

$$\text{SettingScope} = (\text{tenant}, \text{category}, \text{subCategory}, \text{code})$$

### 2.1 坐標定義與欄位對應

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        Setting 語系項目坐標與資料結構                    │
├────────────────────────────────────────────────────────────────────────┤
│ Scope 坐標:                                                            │
│   • tenant      : <TENANT_CODE>  (例如: "CW", "CORP_HQ")               │
│   • category    : "LOCALE"       (固定對應元資料字典的大分類代碼)          │
│   • subCategory : "UI"           (子維度分類，預設固定為 "UI")           │
│   • code        : <LOCALE_CODE>  (標準語系標籤，例如: "zh-TW", "en-US")   │
├────────────────────────────────────────────────────────────────────────┤
│ Profile 與屬性:                                                        │
│   • name        : 語系顯示名稱 (例如: "繁體中文", "English", "日本語")    │
│   • description : 詳細說明備註 (例如: "繁體中文 (台灣)")                 │
│   • value       : 標記預設語系 ("DEFAULT" 代表租戶預設；非預設留空 "")    │
│   • priorityNo  : 下拉選單顯示排序 (1, 2, 3...)                        │
│   • activeFlag  : 有效性狀態 ('Y' 啟用 / 'N' 停用)                      │
└────────────────────────────────────────────────────────────────────────┘
```

> [!IMPORTANT]
> **唯一性約束**：資料庫設定了 `uk_setting_scope (tenant, category, code)` 複合唯一索引。
> 這確保了在同一租戶下，同一個語系代碼（如 `zh-TW`）絕不會重複建立。

---

## 三、如何定義自己 Tenant 的語系

系統提供兩種主要配置途徑：**前端可視化界面** 與 **RESTful API**。

### 方式一：前端界面配置（推薦管理者使用）

1. **點擊頂部地球按鈕**：
   在系統 Header 頂部導航列右側（租戶選擇器旁），點擊帶有地球圖示（🌐）的按鈕。
2. **開啟維護對話框**：
   在彈出的語系 Popover 下拉面板中，點擊底部的 **「維護租戶語系」**。
3. **選擇範本或自行輸入**：
   - **常用範本下拉選單**：支援一鍵帶入常用語系（`繁體中文 (zh-TW)`、`English (en-US)`、`日本語 (ja-JP)`、`簡體中文 (zh-CN)`、`한국어 (ko-KR)`）。
   - **語系唯一代碼 (Code)**：推薦遵循 BCP 47 標準，如 `zh-TW`、`en-US`。
   - **顯示名稱 (Name)**：此租戶使用者在語系選單中看見的名稱。
   - **詳細說明 (Description)**：輔助備註說明。
   - **顯示排序號 (Priority No)**：控制選單排列先後順序（小數字排在前面）。
   - **設為預設 (Default)**：勾選後將作為該租戶新使用者的預設系統語言。
4. **點擊「建立語系」**：
   表單提交後立即寫入後端資料庫，並即時反映在地球選單與租戶語系列表中。
5. **後續維護管理**：
   在對話框上方表格中，可隨時點擊「設為預設」、「編輯（鉛筆）」或「刪除（垃圾桶）」。

---

### 方式二：透過 RESTful API 定義與維護

適用於系統初始化腳本、CI/CD 自動化或與第三方 SSO 帳號系統對接。

#### 步驟 1：確保 `LOCALE` 維度元資料已註冊
通常系統啟動時已由 `SettingDataInitializer` 自動註冊。若為全新環境，可調用：
```http
POST /api/v1/categories
Content-Type: application/json

{
  "tenant": "CW",
  "code": "LOCALE",
  "name": "系統語系配置",
  "description": "管理各租戶支援的系統語言環境與國際化設定",
  "priorityNo": 2
}
```

---

#### 步驟 2：為指定租戶新增支援語系
使用標準業務設定新增端點 `POST /api/v1/settings`：

**新增繁體中文（並設為預設）：**
```bash
curl -X POST http://localhost:8080/api/v1/settings \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: ENTERPRISE_A" \
  -H "X-User-Id: admin@enterprise.com" \
  -d '{
    "tenant": "ENTERPRISE_A",
    "category": "LOCALE",
    "subCategory": "UI",
    "code": "zh-TW",
    "name": "繁體中文",
    "description": "繁體中文 (台灣)",
    "value": "繁體中文 (台灣)",
    "priorityNo": 1
  }'
```

**新增英文支援：**
```bash
curl -X POST http://localhost:8080/api/v1/settings \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: ENTERPRISE_A" \
  -H "X-User-Id: admin@enterprise.com" \
  -d '{
    "tenant": "ENTERPRISE_A",
    "category": "LOCALE",
    "subCategory": "UI",
    "code": "en-US",
    "name": "English",
    "description": "English (United States)",
    "value": "",
    "priorityNo": 2
  }'
```

---

#### 步驟 3：切換指定語系為租戶預設語系
調用專屬原子切換端點，後端將自動將該租戶其他語系的 `DEFAULT` 標記清除：
```http
PUT /api/v1/settings/locales/{settingId}/default
X-Tenant-Id: ENTERPRISE_A
```

**回應範例：**
```json
{
  "id": 15,
  "tenant": "ENTERPRISE_A",
  "code": "en-US",
  "name": "English",
  "description": "English (United States)",
  "isDefault": true,
  "priorityNo": 2,
  "activeFlag": "Y"
}
```

---

#### 步驟 4：檢索指定租戶已啟用之語系列表
供前端 UI 或使用者登入時拉取可用語系清單：
```http
GET /api/v1/settings/locales?tenant=ENTERPRISE_A
```

**回應範例（無任何全域回退，純淨返回該租戶自定義清單）：**
```json
[
  {
    "id": 14,
    "tenant": "ENTERPRISE_A",
    "code": "zh-TW",
    "name": "繁體中文",
    "description": "繁體中文 (台灣)",
    "isDefault": false,
    "priorityNo": 1,
    "activeFlag": "Y"
  },
  {
    "id": 15,
    "tenant": "ENTERPRISE_A",
    "code": "en-US",
    "name": "English",
    "description": "English (United States)",
    "isDefault": true,
    "priorityNo": 2,
    "activeFlag": "Y"
  }
]
```

> [!NOTE]
> 若新租戶尚未配置任何語系，端點將返回空陣列 `[]`，前端 Header 會提示「未設定語系」，引導管理員點擊配置。

---

## 四、常見問題與注意事項 (FAQ)

### Q1：為什麼移除全域 `ALL` 語系回退？
**答**：在真實企業多租戶場景中，不同跨國客戶有嚴格的語系合約邊界（例如日本客戶不希望介面出現未經完整在地化翻譯的簡體中文或德文選項）。
將全域回退移除後，每個租戶自主掌控語系列表，徹底杜絕了不同租戶間的非預期語言干擾。

### Q2：租戶可以有多個「預設語系」嗎？
**答**：不可以。每個租戶在同一時間僅能存在**一個** `value = "DEFAULT"` 的語系。
當管理員透過前端或呼叫 `/locales/{id}/default` 端點時，後端會自動保證排他性。

### Q3：如果想暫時停用某個語系但不想刪除？
**答**：在維護表格中點擊編輯，將「有效性狀態」調整為 `停用 (N)` 即可。
停用後的語系不會出現在使用者的地球切換選單中，但依然保留於維護彈窗的歷史清單中供日後隨時恢復。

### Q4：什麼情況下會限制停用或刪除語系？
**答**：為避免系統產生預設語言真空或介面崩潰，系統設有**三重防呆限制**（前後端共同防護）：
1. **租戶預設語系 (DEFAULT)**：標記為預設的語系禁止停用與刪除；若需停用，請先指派其他有效語系為預設。
2. **當前使用者使用中語系**：管理者目前登入瀏覽正選用該語言時禁止停用/刪除，需先切換至其他語言。
3. **最後一個有效語系**：租戶必須至少保留一種有效語系，不允許將最後一個啟用語系停用或刪除。
